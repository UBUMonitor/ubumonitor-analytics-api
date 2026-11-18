package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.ItemnameApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableDataItemApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.jsoup.Jsoup;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterTableResponseMapper {

  String CATEGORY_PREFIX = "cat_";
  String GRADE_ITEM_PREFIX = "row_";
  String CATEGORY_TYPE = "category";
  String COURSE_TYPE = "course";

  // ---------------------------------------------------------------------------------------------
  // API pública
  // ---------------------------------------------------------------------------------------------

  default void fillParent(
      GetGradesTableResponseApi response,
      List<GradeItem> itemsWithoutParent,
      List<GradeItem> allGradeItems) {
    Map<Long, Long> itemIdByInstance = indexContainerIdsByInstance(allGradeItems);
    List<TableDataItemApi> tableData = firstTableData(response);

    addMissingParentCategories(tableData, itemsWithoutParent, allGradeItems, itemIdByInstance);
    fillGradeRanges(tableData, allGradeItems);

    Map<Long, Long> parentIdByItemId =
        parentIdsByRowId(tableData, this::isGradeItem, itemIdByInstance);
    Map<Long, Long> parentIdByCategoryInstance =
        parentIdsByRowId(tableData, this::isCategory, itemIdByInstance);

    for (GradeItem item : itemsWithoutParent) {
      if (item.getParentId() != null) {
        continue;
      }
      Long parentId =
          resolveParentId(item, parentIdByItemId, parentIdByCategoryInstance, itemIdByInstance);
      if (parentId != null) {
        item.setParentId(parentId);
      }
    }
  }

  default List<TableDataItemApi> firstTableData(GetGradesTableResponseApi response) {
    return Optional.ofNullable(response)
        .map(GetGradesTableResponseApi::getTables)
        .flatMap(tables -> tables.stream().findFirst())
        .map(TableApi::getTabledata)
        .orElse(List.of());
  }

  default boolean isCategory(TableDataItemApi row) {
    return itemnameIdStartsWith(row, CATEGORY_PREFIX);
  }

  default boolean isGradeItem(TableDataItemApi row) {
    return itemnameIdStartsWith(row, GRADE_ITEM_PREFIX);
  }

  default boolean isCategory(GradeItem item) {
    return CATEGORY_TYPE.equals(item.getItemType());
  }

  /** El id tiene el formato "cat_82_13": tipo (cat/item), id del elemento y id de usuario. */
  default Long parseId(TableDataItemApi row) {
    String rawId =
        Optional.ofNullable(row)
            .map(TableDataItemApi::getItemname)
            .map(ItemnameApi::getId)
            .orElse(null);
    if (rawId == null) {
      return null;
    }
    try {
      return Long.valueOf(rawId.split("_")[1]);
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException _) {
      return null;
    }
  }

  default Long findParentId(TableDataItemApi row, Map<Long, Long> itemIdByInstance) {
    List<Long> parentCategories = row.getParentcategories();
    if (parentCategories == null) {
      return null;
    }
    for (int index = parentCategories.size() - 1; index >= 0; index--) {
      Long parentId = itemIdByInstance.get(parentCategories.get(index));
      if (parentId != null) {
        return parentId;
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------------------------
  // Resolución de padres
  // ---------------------------------------------------------------------------------------------

  /** Mapea instancia de categoría/curso -> id del GradeItem que la representa. */
  private Map<Long, Long> indexContainerIdsByInstance(List<GradeItem> gradeItems) {
    Map<Long, Long> itemIdByInstance = new HashMap<>();
    for (GradeItem item : gradeItems) {
      if (isContainer(item) && item.getItemInstance() != null) {
        itemIdByInstance.putIfAbsent(item.getItemInstance(), item.getId());
      }
    }
    return itemIdByInstance;
  }

  private boolean isContainer(GradeItem item) {
    return isCategory(item) || COURSE_TYPE.equals(item.getItemType());
  }

  /** Mapea el id numérico de cada fila (filtrada) -> id del GradeItem padre. */
  private Map<Long, Long> parentIdsByRowId(
      List<TableDataItemApi> tableData,
      Predicate<TableDataItemApi> rowFilter,
      Map<Long, Long> itemIdByInstance) {
    Map<Long, Long> parentIds = new HashMap<>();
    for (TableDataItemApi row : tableData) {
      if (!rowFilter.test(row)) {
        continue;
      }
      Long rowId = parseId(row);
      if (rowId != null) {
        parentIds.put(rowId, findParentId(row, itemIdByInstance));
      }
    }
    return parentIds;
  }

  private Long resolveParentId(
      GradeItem item,
      Map<Long, Long> parentIdByItemId,
      Map<Long, Long> parentIdByCategoryInstance,
      Map<Long, Long> itemIdByInstance) {
    if (isCategory(item)) {
      return parentIdByCategoryInstance.get(item.getItemInstance());
    }
    Long parentId = parentIdByItemId.get(item.getId());
    Long parentCategoryId = item.getParentCategoryId();
    return parentCategoryId == null
        ? parentId
        : itemIdByInstance.getOrDefault(parentCategoryId, parentId);
  }

  // ---------------------------------------------------------------------------------------------
  // Categorías sintéticas
  // ---------------------------------------------------------------------------------------------

  private void addMissingParentCategories(
      List<TableDataItemApi> tableData,
      List<GradeItem> itemsWithoutParent,
      List<GradeItem> allGradeItems,
      Map<Long, Long> itemIdByInstance) {
    for (GradeItem gradeItem : itemsWithoutParent) {
      Long categoryId = gradeItem.getParentCategoryId();
      if (isCategory(gradeItem) || categoryId == null || itemIdByInstance.containsKey(categoryId)) {
        continue;
      }

      TableDataItemApi categoryRow = categoryRow(categoryId, tableData);
      GradeItem category = toSyntheticCategory(categoryRow);
      if (category == null) {
        continue;
      }

      category.setParentId(findParentId(categoryRow, itemIdByInstance));
      allGradeItems.add(indexOfFirstChild(allGradeItems, categoryId), category);
      itemIdByInstance.put(categoryId, category.getId());
    }
  }

  /** Posición del primer hijo de la categoría, o el final de la lista si no tiene ninguno. */
  private int indexOfFirstChild(List<GradeItem> gradeItems, Long categoryId) {
    for (int index = 0; index < gradeItems.size(); index++) {
      if (categoryId.equals(gradeItems.get(index).getParentCategoryId())) {
        return index;
      }
    }
    return gradeItems.size();
  }

  private TableDataItemApi categoryRow(Long categoryId, List<TableDataItemApi> tableData) {
    for (TableDataItemApi row : tableData) {
      if (isCategory(row) && categoryId.equals(parseId(row))) {
        return row;
      }
    }
    return null;
  }

  private GradeItem toSyntheticCategory(TableDataItemApi row) {
    if (row == null || row.getItemname() == null) {
      return null;
    }
    Long categoryId = parseId(row);
    if (categoryId == null) {
      return null;
    }

    GradeItem category = new GradeItem();
    category.setId(-categoryId);
    category.setItemType(CATEGORY_TYPE);
    category.setItemInstance(categoryId);
    category.setCategoryId(categoryId);
    category.setParentCategoryId(last(row.getParentcategories()));
    category.setName(categoryName(row));
    return category;
  }

  private String categoryName(TableDataItemApi row) {
    String content = row.getItemname().getContent();
    if (content == null) {
      return null;
    }
    var categoryLabel = Jsoup.parse(content).select(".category-content > span").last();
    return categoryLabel == null ? null : categoryLabel.text();
  }

  private Long last(List<Long> values) {
    return values == null || values.isEmpty() ? null : values.getLast();
  }

  // ---------------------------------------------------------------------------------------------
  // Rangos de calificación
  // ---------------------------------------------------------------------------------------------

  private void fillGradeRanges(List<TableDataItemApi> tableData, List<GradeItem> gradeItems) {
    Map<Long, GradeItem> gradeItemsById = new HashMap<>();
    Map<Long, GradeItem> categoriesByInstance = new HashMap<>();
    for (GradeItem item : gradeItems) {
      if (item.getId() != null) {
        gradeItemsById.putIfAbsent(item.getId(), item);
      }
      if (isCategory(item) && item.getItemInstance() != null) {
        categoriesByInstance.putIfAbsent(item.getItemInstance(), item);
      }
    }

    for (TableDataItemApi row : tableData) {
      GradeRange range = numericRange(row);
      if (range == null) {
        continue;
      }
      GradeItem gradeItem = gradeItemForRow(row, gradeItemsById, categoriesByInstance);
      if (gradeItem == null) {
        continue;
      }
      if (gradeItem.getGradeMin() == null) {
        gradeItem.setGradeMin(range.min());
      }
      if (gradeItem.getGradeMax() == null) {
        gradeItem.setGradeMax(range.max());
      }
    }
  }

  private GradeItem gradeItemForRow(
      TableDataItemApi row,
      Map<Long, GradeItem> gradeItemsById,
      Map<Long, GradeItem> categoriesByInstance) {
    Long id = parseId(row);
    if (id == null) {
      return null;
    }
    if (isCategory(row)) {
      return categoriesByInstance.get(id);
    }
    return isGradeItem(row) ? gradeItemsById.get(id) : null;
  }

  /** Devuelve el rango numérico de la fila ("0–100"), o null si no aplica. */
  private GradeRange numericRange(TableDataItemApi row) {
    if (row == null || row.getRange() == null || row.getRange().getContent() == null) {
      return null;
    }
    String range = MapperUtils.parseHtml(row.getRange().getContent()).trim();
    int separator = range.indexOf('–');
    if (separator < 0) {
      separator = range.indexOf('-', 1);
    }
    if (separator < 0) {
      return null;
    }

    BigDecimal min = MapperUtils.parseNumber(range.substring(0, separator));
    BigDecimal max = MapperUtils.parseNumber(range.substring(separator + 1));
    return min == null || max == null ? null : new GradeRange(min, max);
  }

  record GradeRange(BigDecimal min, BigDecimal max) {}

  // ---------------------------------------------------------------------------------------------
  // Utilidades
  // ---------------------------------------------------------------------------------------------

  private boolean itemnameIdStartsWith(TableDataItemApi row, String prefix) {
    return Optional.ofNullable(row)
        .map(TableDataItemApi::getItemname)
        .map(ItemnameApi::getId)
        .map(id -> id.startsWith(prefix))
        .orElse(false);
  }
}
