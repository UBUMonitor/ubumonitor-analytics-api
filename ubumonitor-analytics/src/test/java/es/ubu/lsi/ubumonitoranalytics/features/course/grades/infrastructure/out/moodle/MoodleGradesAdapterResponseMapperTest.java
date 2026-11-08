package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void assignsPositionsFromRootFirstHierarchyWhenCourseItemIsLast() {
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
    assertEquals(List.of(0, 1, 2, 3, 4), mappedItems.stream().map(GradeItem::getPosition).toList());
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
