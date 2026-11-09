package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MoodleGradesAdapterResponseMapperTest {

  private final MoodleGradesAdapterResponseMapper mapper =
      Mappers.getMapper(MoodleGradesAdapterResponseMapper.class);

  @Test
  void assignsPositionZeroToCourseRootAndFollowingPositionsToItsChildren() {
    GradeItemApi childOfCategoryB = gradeItem(50L, "mod", 900L, 700L, null);
    GradeItemApi categoryB = gradeItem(30L, "category", 700L, null, 500L);
    GradeItemApi childOfCategoryA = gradeItem(40L, "mod", 901L, 600L, null);
    GradeItemApi categoryA = gradeItem(20L, "category", 600L, null, 500L);
    GradeItemApi course = gradeItem(10L, "course", 500L, null, null);
    List<GradeItemApi> sourceOrder =
        List.of(childOfCategoryB, categoryB, childOfCategoryA, categoryA, course);

    UserGradeApi userGrade = new UserGradeApi();
    userGrade.setGradeitems(sourceOrder);
    GetGradeItemsResponseApi response = new GetGradeItemsResponseApi();
    response.setUsergrades(List.of(userGrade));

    List<GradeItem> mappedItems = mapper.toGradeItems(response);

    assertEquals(
        List.of(10L, 30L, 50L, 20L, 40L), mappedItems.stream().map(GradeItem::getId).toList());
    assertEquals(500L, mappedItems.getFirst().getCategoryId());
    assertEquals(700L, mappedItems.get(1).getCategoryId());
    assertEquals(500L, mappedItems.get(1).getParentCategoryId());
    assertEquals(null, mappedItems.get(2).getCategoryId());
    assertTrue(mappedItems.stream().allMatch(gradeItem -> gradeItem.getPosition() == null));

    mapper.assignPositions(mappedItems);

    assertEquals(List.of(0, 1, 2, 3, 4), mappedItems.stream().map(GradeItem::getPosition).toList());
  }

  @Test
  void usesNegativeCategoryIdWhenGradeItemIsNotAvailableFromApi() {
    GradeItemApi gradeItem = gradeItem(50L, "mod", 900L, 700L, null);
    gradeItem.setParentcategoryid(600L);
    GradeItemApi category = gradeItem(30L, "category", 700L, null, 600L);
    UserGradeApi availableItems = new UserGradeApi();
    availableItems.setUserid(21L);
    availableItems.setGradeitems(List.of(category, gradeItem));
    GradeItemApi itemNotAvailable = gradeItem(99L, "manual", null, 700L, null);
    UserGradeApi gradeWithUnavailableItem = new UserGradeApi();
    gradeWithUnavailableItem.setUserid(22L);
    gradeWithUnavailableItem.setGradeitems(List.of(itemNotAvailable));
    GetGradeItemsResponseApi response = new GetGradeItemsResponseApi();
    response.setUsergrades(List.of(availableItems, gradeWithUnavailableItem));

    var mappedUserGrades = mapper.toUserGrades(response);
    List<GradeItem> mappedItems = mapper.toGradeItems(response);
    GradeItem mappedItem =
        mappedItems.stream().filter(item -> item.getId().equals(50L)).findFirst().orElseThrow();

    assertEquals(
        List.of(30L, 50L, -700L),
        mappedUserGrades.stream().map(userGrade -> userGrade.getGradeItemId()).toList());
    assertEquals(null, mappedItem.getCategoryId());
    assertEquals(700L, mappedItem.getParentCategoryId());
    GradeItem mappedCategory =
        mappedItems.stream().filter(item -> item.getId().equals(30L)).findFirst().orElseThrow();
    assertEquals(700L, mappedCategory.getCategoryId());
    assertEquals(600L, mappedCategory.getParentCategoryId());
  }

  private GradeItemApi gradeItem(
      Long id, String itemType, Long itemInstance, Long categoryId, Long parentCategoryId) {
    GradeItemApi gradeItem = new GradeItemApi();
    gradeItem.setId(id);
    gradeItem.setItemtype(itemType);
    gradeItem.setIteminstance(itemInstance);
    gradeItem.setCategoryid(categoryId);
    gradeItem.setParentcategoryid(parentCategoryId);
    return gradeItem;
  }
}
