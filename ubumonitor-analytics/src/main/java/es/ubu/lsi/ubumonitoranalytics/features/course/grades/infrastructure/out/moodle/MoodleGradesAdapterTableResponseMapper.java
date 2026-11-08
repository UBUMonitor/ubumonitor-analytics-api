package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.ItemnameApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableDataItemApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterTableResponseMapper {

  String CATEGORY_PREFIX = "cat_";
  String GRADE_ITEM_PREFIX = "row_";

  default void fillParent(
      GetGradesTableResponseApi response,
      List<GradeItem> itemsWithoutParent,
      List<GradeItem> allGradeItems) {
    Map<Long, Long> itemIdByInstance = new HashMap<>();
    for (GradeItem item : allGradeItems) {
      if ((isCategory(item) || "course".equals(item.getItemType()))
          && item.getItemInstance() != null) {
        itemIdByInstance.putIfAbsent(item.getItemInstance(), item.getId());
      }
    }

    Map<Long, Long> parentIdsByItemId = new HashMap<>();
    Map<Long, Long> parentIdsByCategoryInstance = new HashMap<>();
    for (TableDataItemApi row : firstTableData(response)) {
      if (isCategory(row)) {
        parentIdsByCategoryInstance.put(parseId(row), findParentId(row, itemIdByInstance));
      } else if (isGradeItem(row)) {
        parentIdsByItemId.put(parseId(row), findParentId(row, itemIdByInstance));
      }
    }

    for (GradeItem item : itemsWithoutParent) {
      Long parentId =
          isCategory(item)
              ? parentIdsByCategoryInstance.get(item.getItemInstance())
              : parentIdsByItemId.get(item.getId());
      if (parentId != null && item.getParentId() == null) {
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
    return Optional.ofNullable(row)
        .map(TableDataItemApi::getItemname)
        .map(ItemnameApi::getId)
        .map(id -> id.startsWith(CATEGORY_PREFIX))
        .orElse(false);
  }

  default boolean isGradeItem(TableDataItemApi row) {
    return Optional.ofNullable(row)
        .map(TableDataItemApi::getItemname)
        .map(ItemnameApi::getId)
        .map(id -> id.startsWith(GRADE_ITEM_PREFIX))
        .orElse(false);
  }

  default boolean isCategory(GradeItem item) {
    return "category".equals(item.getItemType());
  }

  /** El id tiene el formato "cat_82_13": tipo (cat/item), id del elemento y id de usuario. */
  default Long parseId(TableDataItemApi row) {
    try {
      return Long.valueOf(row.getItemname().getId().split("_")[1]);
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException _) {
      return 0L;
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
}
