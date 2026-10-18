package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.ItemnameApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableDataItemApi;
import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.*;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterTableResponseMapper {

  String CATEGORY_PREFIX = "cat_";

  default void fillParent(GetGradesTableResponseApi response, List<GradeItem> categories) {
    Set<Integer> knownIds =
        categories.stream().map(GradeItem::getItemInstance).collect(Collectors.toSet());

    Map<Integer, Integer> parentIds = new HashMap<>();
    firstTableData(response).stream()
        .filter(this::isCategory)
        .forEach(row -> parentIds.put(parseId(row), findParentId(row, knownIds)));

    categories.forEach(item -> item.setParentId(parentIds.get(item.getItemInstance())));
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

  /** El id tiene el formato "cat_82_13": tipo (cat/item), id del elemento y id de usuario. */
  default Integer parseId(TableDataItemApi row) {
    try {
      return Integer.valueOf(row.getItemname().getId().split("_")[1]);
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException _) {
      return 0;
    }
  }

  default Integer findParentId(TableDataItemApi row, Set<Integer> knownIds) {
    return Optional.ofNullable(row.getParentcategories())
        .filter(e -> !e.isEmpty())
        .flatMap(parents -> parents.stream().findFirst())
        .filter(knownIds::contains)
        .orElse(null);
  }
}
