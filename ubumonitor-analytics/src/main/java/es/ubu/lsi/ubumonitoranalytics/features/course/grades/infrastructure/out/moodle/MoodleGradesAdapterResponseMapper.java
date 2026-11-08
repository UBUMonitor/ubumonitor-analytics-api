package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterResponseMapper {

  String COURSE_ITEM_TYPE = "course";
  String CATEGORY_ITEM_TYPE = "category";

  int PERCENTAGE_SCALE = 2;
  int ONE_HUNDRED = 100;

  // ---------------------------------------------------------------------------------------------
  // Course grade
  // ---------------------------------------------------------------------------------------------

  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "gradeItems", source = "getGradeItemsResponseApi")
  @Mapping(target = "userGrades", source = "getGradeItemsResponseApi")
  CourseGrade toCourseGradeItems(Long courseId, GetGradeItemsResponseApi getGradeItemsResponseApi);

  // ---------------------------------------------------------------------------------------------
  // Grade items
  // ---------------------------------------------------------------------------------------------

  /**
   * Maps the grade items of the first user grade that contains items, sorted by hierarchy (course
   * item first, then each category followed by its children).
   */
  default List<GradeItem> toGradeItems(GetGradeItemsResponseApi getGradeItemsResponseApi) {
    return firstGradeItems(getGradeItemsResponseApi)
        .map(
            gradeItems -> {
              List<GradeItemApi> orderedGradeItems = hierarchyOrder(gradeItems);
              return orderedGradeItems.stream()
                  .map(gradeItem -> toGradeItem(gradeItem, orderedGradeItems))
                  .toList();
            })
        .orElseGet(List::of);
  }

  @Mapping(target = "id", source = "id")
  @Mapping(target = "parentId", source = "gradeItemApi", qualifiedByName = "parentId")
  @Mapping(target = "position", source = "gradeItemApi", qualifiedByName = "position")
  @Mapping(target = "itemInstance", source = "iteminstance")
  @Mapping(target = "moduleId", source = "cmid")
  @Mapping(target = "itemType", source = "itemtype")
  @Mapping(target = "name", source = "itemname")
  @Mapping(target = "gradeMax", source = "grademax")
  @Mapping(target = "gradeMin", source = "grademin")
  GradeItem toGradeItem(GradeItemApi gradeItemApi, @Context List<GradeItemApi> allGradeItems);

  @Named("parentId")
  default Long parentId(GradeItemApi gradeItemApi, @Context List<GradeItemApi> allGradeItems) {
    String itemType = gradeItemApi.getItemtype();
    if (COURSE_ITEM_TYPE.equals(itemType)) {
      return null;
    }
    Object parentInstance =
        CATEGORY_ITEM_TYPE.equals(itemType)
            ? gradeItemApi.getParentcategoryid()
            : gradeItemApi.getCategoryid();
    return findItemIdByInstance(allGradeItems, parentInstance);
  }

  @Named("position")
  default Integer position(GradeItemApi gradeItemApi, @Context List<GradeItemApi> allGradeItems) {
    return allGradeItems.indexOf(gradeItemApi);
  }

  // ---------------------------------------------------------------------------------------------
  // User grades
  // ---------------------------------------------------------------------------------------------

  default List<UserGrade> toUserGrades(GetGradeItemsResponseApi getGradeItemsResponseApi) {
    if (getGradeItemsResponseApi == null || getGradeItemsResponseApi.getUsergrades() == null) {
      return List.of();
    }
    return getGradeItemsResponseApi.getUsergrades().stream()
        .filter(Objects::nonNull)
        .filter(userGradeApi -> userGradeApi.getGradeitems() != null)
        .flatMap(
            userGradeApi ->
                userGradeApi.getGradeitems().stream()
                    .filter(Objects::nonNull)
                    .map(gradeItemApi -> toUserGrade(userGradeApi, gradeItemApi)))
        .toList();
  }

  @Mapping(target = "userId", source = "userGradeApi.userid")
  @Mapping(target = "gradeItemId", source = "gradeItemApi.id")
  @Mapping(target = "gradeRaw", source = "gradeItemApi.graderaw")
  @Mapping(
      target = "gradePercentageRaw",
      source = "gradeItemApi",
      qualifiedByName = "gradePercentageRaw")
  UserGrade toUserGrade(UserGradeApi userGradeApi, GradeItemApi gradeItemApi);

  @Named("gradePercentageRaw")
  default BigDecimal gradePercentageRaw(GradeItemApi gradeItemApi) {
    Double gradeRaw = gradeItemApi.getGraderaw();
    Double min = gradeItemApi.getGrademin();
    Double max = gradeItemApi.getGrademax();
    if (gradeRaw == null || min == null || max == null || Double.compare(max, min) == 0) {
      return null;
    }
    double percentage = (gradeRaw - min) / (max - min) * ONE_HUNDRED;
    return BigDecimal.valueOf(percentage).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
  }

  // ---------------------------------------------------------------------------------------------
  // Hierarchy helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Sorts the items depth-first: course items first, then the remaining root items, and finally any
   * orphan item (so that no item is lost even if the hierarchy is inconsistent).
   */
  default List<GradeItemApi> hierarchyOrder(List<GradeItemApi> gradeItems) {
    List<GradeItemApi> items = gradeItems.stream().filter(Objects::nonNull).toList();

    Map<Long, List<GradeItemApi>> childrenByParentId = new HashMap<>();
    List<GradeItemApi> courseItems = new ArrayList<>();
    List<GradeItemApi> rootItems = new ArrayList<>();

    for (GradeItemApi item : items) {
      if (COURSE_ITEM_TYPE.equals(item.getItemtype())) {
        courseItems.add(item);
      }
      Long parentId = parentId(item, items);
      if (parentId == null) {
        rootItems.add(item);
      } else {
        childrenByParentId.computeIfAbsent(parentId, key -> new ArrayList<>()).add(item);
      }
    }

    List<GradeItemApi> ordered = new ArrayList<>(items.size());
    Set<GradeItemApi> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    courseItems.forEach(item -> appendHierarchy(item, childrenByParentId, visited, ordered));
    rootItems.forEach(item -> appendHierarchy(item, childrenByParentId, visited, ordered));
    items.forEach(item -> appendHierarchy(item, childrenByParentId, visited, ordered));
    return ordered;
  }

  private static void appendHierarchy(
      GradeItemApi gradeItem,
      Map<Long, List<GradeItemApi>> childrenByParentId,
      Set<GradeItemApi> visited,
      List<GradeItemApi> ordered) {
    if (!visited.add(gradeItem)) {
      return;
    }
    ordered.add(gradeItem);
    childrenByParentId
        .getOrDefault(gradeItem.getId(), List.of())
        .forEach(child -> appendHierarchy(child, childrenByParentId, visited, ordered));
  }

  private static Long findItemIdByInstance(List<GradeItemApi> gradeItems, Object itemInstance) {
    return gradeItems.stream()
        .filter(candidate -> Objects.equals(candidate.getIteminstance(), itemInstance))
        .findFirst()
        .map(GradeItemApi::getId)
        .orElse(null);
  }

  private static Optional<List<GradeItemApi>> firstGradeItems(
      GetGradeItemsResponseApi getGradeItemsResponseApi) {
    if (getGradeItemsResponseApi == null || getGradeItemsResponseApi.getUsergrades() == null) {
      return Optional.empty();
    }
    return getGradeItemsResponseApi.getUsergrades().stream()
        .filter(Objects::nonNull)
        .map(UserGradeApi::getGradeitems)
        .filter(Objects::nonNull)
        .findFirst();
  }
}
