package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterResponseMapper {

  String COURSE_ITEM_TYPE = "course";
  String CATEGORY_ITEM_TYPE = "category";

  int PERCENTAGE_SCALE = 2;
  int ONE_HUNDRED = 100;

  // ---------------------------------------------------------------------------------------------
  // Course grade
  // ---------------------------------------------------------------------------------------------

  @Mapping(target = "gradeItems", source = "getGradeItemsResponseApi")
  @Mapping(target = "userGrades", source = "getGradeItemsResponseApi")
  CourseGrade toCourseGradeItems(Long courseId, GetGradeItemsResponseApi getGradeItemsResponseApi);

  /**
   * Assigns a zero-based {@code position} to each item following the order of the list. Null items
   * are skipped and do not consume a position.
   */
  default void assignPositions(List<GradeItem> gradeItems) {
    if (gradeItems == null) {
      return;
    }
    int position = 0;
    for (GradeItem gradeItem : gradeItems) {
      if (gradeItem != null) {
        gradeItem.setPosition(position++);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Grade items
  // ---------------------------------------------------------------------------------------------

  /**
   * Maps the grade items of the first user grade that contains items, sorted by hierarchy (course
   * item first, then each category followed by its children).
   */
  default List<GradeItem> toGradeItems(GetGradeItemsResponseApi response) {
    List<GradeItemApi> items =
        firstGradeItems(response).stream().flatMap(List::stream).filter(Objects::nonNull).toList();
    Map<Object, Long> containerIds = containerIdsByInstance(items);

    return hierarchyOrder(items, containerIds).stream()
        .map(item -> toGradeItem(item, containerIds))
        .toList();
  }

  @Mapping(target = "position", ignore = true)
  @Mapping(target = "parentId", source = "gradeItemApi", qualifiedByName = "parentId")
  @Mapping(target = "itemInstance", source = "iteminstance")
  @Mapping(target = "categoryId", source = "gradeItemApi", qualifiedByName = "categoryId")
  @Mapping(
      target = "parentCategoryId",
      source = "gradeItemApi",
      qualifiedByName = "parentCategoryId")
  @Mapping(target = "moduleId", source = "cmid")
  @Mapping(target = "itemType", source = "itemtype")
  @Mapping(target = "name", source = "itemname")
  @Mapping(target = "gradeMax", source = "grademax")
  @Mapping(target = "gradeMin", source = "grademin")
  GradeItem toGradeItem(GradeItemApi gradeItemApi, @Context Map<Object, Long> containerIds);

  @Named("parentId")
  default Long parentId(GradeItemApi gradeItemApi, @Context Map<Object, Long> containerIds) {
    return parentIdOf(gradeItemApi, containerIds);
  }

  @Named("categoryId")
  default Long categoryId(GradeItemApi gradeItemApi) {
    if (isCategoryContainer(gradeItemApi)) {
      return gradeItemApi.getIteminstance();
    }
    return null;
  }

  @Named("parentCategoryId")
  default Long parentCategoryId(GradeItemApi gradeItemApi) {
    if (CATEGORY_ITEM_TYPE.equals(gradeItemApi.getItemtype())) {
      return gradeItemApi.getParentcategoryid();
    }
    if (!COURSE_ITEM_TYPE.equals(gradeItemApi.getItemtype())) {
      return gradeItemApi.getCategoryid();
    }
    return null;
  }

  // ---------------------------------------------------------------------------------------------
  // User grades
  // ---------------------------------------------------------------------------------------------

  default List<UserGrade> toUserGrades(GetGradeItemsResponseApi response) {
    if (response == null || response.getUsergrades() == null) {
      return List.of();
    }
    Set<Long> availableGradeItemIds = availableGradeItemIds(response);
    return response.getUsergrades().stream()
        .filter(Objects::nonNull)
        .flatMap(
            userGradeApi ->
                nonNullItems(userGradeApi)
                    .map(
                        item -> toUserGradeWithFallback(userGradeApi, item, availableGradeItemIds)))
        .toList();
  }

  default Set<Long> availableGradeItemIds(GetGradeItemsResponseApi response) {
    Set<Long> ids = new HashSet<>();
    firstGradeItems(response)
        .ifPresent(
            items -> {
              for (GradeItemApi item : items) {
                if (item != null && item.getId() != null) {
                  ids.add(item.getId());
                }
              }
            });
    return ids;
  }

  default UserGrade toUserGradeWithFallback(
      UserGradeApi userGradeApi, GradeItemApi gradeItemApi, Set<Long> availableGradeItemIds) {
    UserGrade userGrade = toUserGrade(userGradeApi, gradeItemApi);
    if (!availableGradeItemIds.contains(gradeItemApi.getId())
        && !isCategoryContainer(gradeItemApi)
        && gradeItemApi.getCategoryid() != null) {
      userGrade.setGradeItemId(-gradeItemApi.getCategoryid());
    }
    return userGrade;
  }

  private static boolean isCategoryContainer(GradeItemApi gradeItemApi) {
    return COURSE_ITEM_TYPE.equals(gradeItemApi.getItemtype())
        || CATEGORY_ITEM_TYPE.equals(gradeItemApi.getItemtype());
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
    Double raw = gradeItemApi.getGraderaw();
    Double min = gradeItemApi.getGrademin();
    Double max = gradeItemApi.getGrademax();
    if (raw == null || min == null || max == null || Double.compare(max, min) == 0) {
      return null;
    }
    double percentage = (raw - min) / (max - min) * ONE_HUNDRED;
    return BigDecimal.valueOf(percentage).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
  }

  // ---------------------------------------------------------------------------------------------
  // Hierarchy helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Indexes course and category items by their {@code iteminstance}. Only these item types can be
   * parents, so instances of other types (e.g. modules) can never be mistaken for a parent.
   */
  private static Map<Object, Long> containerIdsByInstance(List<GradeItemApi> items) {
    Map<Object, Long> containerIds = new HashMap<>();
    for (GradeItemApi item : items) {
      String type = item.getItemtype();
      if (COURSE_ITEM_TYPE.equals(type) || CATEGORY_ITEM_TYPE.equals(type)) {
        containerIds.putIfAbsent(item.getIteminstance(), item.getId());
      }
    }
    return containerIds;
  }

  private static Long parentIdOf(GradeItemApi item, Map<Object, Long> containerIds) {
    return switch (String.valueOf(item.getItemtype())) {
      case COURSE_ITEM_TYPE -> null;
      case CATEGORY_ITEM_TYPE -> containerIds.get(item.getParentcategoryid());
      default -> containerIds.get(item.getCategoryid());
    };
  }

  /**
   * Sorts the items depth-first: course items first, then the remaining root items, and finally any
   * orphan item (so that no item is lost even if the hierarchy is inconsistent).
   */
  private static List<GradeItemApi> hierarchyOrder(
      List<GradeItemApi> items, Map<Object, Long> containerIds) {
    Map<Long, List<GradeItemApi>> childrenByParentId = new HashMap<>();
    List<GradeItemApi> courseItems = new ArrayList<>();
    List<GradeItemApi> rootItems = new ArrayList<>();

    for (GradeItemApi item : items) {
      if (COURSE_ITEM_TYPE.equals(item.getItemtype())) {
        courseItems.add(item);
      }
      Long parentId = parentIdOf(item, containerIds);
      if (parentId == null) {
        rootItems.add(item);
      } else {
        childrenByParentId.computeIfAbsent(parentId, _ -> new ArrayList<>()).add(item);
      }
    }

    List<GradeItemApi> ordered = new ArrayList<>(items.size());
    Set<GradeItemApi> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    Stream.of(courseItems, rootItems, items)
        .flatMap(List::stream)
        .forEach(item -> appendHierarchy(item, childrenByParentId, visited, ordered));
    return ordered;
  }

  private static void appendHierarchy(
      GradeItemApi item,
      Map<Long, List<GradeItemApi>> childrenByParentId,
      Set<GradeItemApi> visited,
      List<GradeItemApi> ordered) {
    if (!visited.add(item)) {
      return;
    }
    ordered.add(item);
    childrenByParentId
        .getOrDefault(item.getId(), List.of())
        .forEach(child -> appendHierarchy(child, childrenByParentId, visited, ordered));
  }

  private static Optional<List<GradeItemApi>> firstGradeItems(GetGradeItemsResponseApi response) {
    if (response == null || response.getUsergrades() == null) {
      return Optional.empty();
    }
    return response.getUsergrades().stream()
        .filter(Objects::nonNull)
        .map(UserGradeApi::getGradeitems)
        .filter(Objects::nonNull)
        .findFirst();
  }

  private static Stream<GradeItemApi> nonNullItems(UserGradeApi userGradeApi) {
    return userGradeApi.getGradeitems() == null
        ? Stream.empty()
        : userGradeApi.getGradeitems().stream().filter(Objects::nonNull);
  }
}
