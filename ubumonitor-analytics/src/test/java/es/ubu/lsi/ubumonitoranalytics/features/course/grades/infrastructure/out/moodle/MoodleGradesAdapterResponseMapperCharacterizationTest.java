package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MoodleGradesAdapterResponseMapperCharacterizationTest {

  private final MoodleGradesAdapterResponseMapper mapper =
      Mappers.getMapper(MoodleGradesAdapterResponseMapper.class);

  @Test
  void mapsEmptyAndNullResponsesToEmptyLists() {
    assertEquals(List.of(), mapper.toGradeItems(null));
    assertEquals(List.of(), mapper.toUserGrades(null));

    GetGradeItemsResponseApi response = new GetGradeItemsResponseApi();
    response.setUsergrades(List.of());
    assertEquals(List.of(), mapper.toGradeItems(response));
    assertEquals(List.of(), mapper.toUserGrades(response));
  }

  @Test
  void preservesItemsWithDuplicateIds() {
    GradeItemApi first = gradeItem(7L, "manual", null, null);
    first.setItemname("first");
    GradeItemApi second = gradeItem(7L, "manual", null, null);
    second.setItemname("second");

    List<GradeItem> mappedItems = mapper.toGradeItems(response(first, second));

    assertEquals(List.of(7L, 7L), mappedItems.stream().map(GradeItem::getId).toList());
    assertEquals(List.of("first", "second"), mappedItems.stream().map(GradeItem::getName).toList());
  }

  @Test
  void mapsMissingCategoryIdToNullParentCategoryId() {
    GradeItemApi orphan = gradeItem(12L, "manual", null, 999L);

    GradeItem mappedItem = mapper.toGradeItems(response(orphan)).getFirst();

    assertNull(mappedItem.getParentId());
    // Non-category items use Moodle's categoryid field; parentcategoryid is ignored here.
    assertNull(mappedItem.getParentCategoryId());
  }

  @Test
  void retainsEveryItemInAnOrphanedCyclicCategoryHierarchy() {
    GradeItemApi firstCategory = gradeItem(101L, "category", 1L, 2L);
    GradeItemApi secondCategory = gradeItem(102L, "category", 2L, 1L);

    List<GradeItem> mappedItems = mapper.toGradeItems(response(firstCategory, secondCategory));

    assertEquals(List.of(101L, 102L), mappedItems.stream().map(GradeItem::getId).toList());
    assertEquals(List.of(102L, 101L), mappedItems.stream().map(GradeItem::getParentId).toList());
  }

  private GetGradeItemsResponseApi response(GradeItemApi... items) {
    UserGradeApi userGrade = new UserGradeApi();
    userGrade.setGradeitems(List.of(items));
    GetGradeItemsResponseApi response = new GetGradeItemsResponseApi();
    response.setUsergrades(List.of(userGrade));
    return response;
  }

  private GradeItemApi gradeItem(Long id, String type, Long instance, Long parentCategoryId) {
    GradeItemApi item = new GradeItemApi();
    item.setId(id);
    item.setItemtype(type);
    item.setIteminstance(instance);
    item.setParentcategoryid(parentCategoryId);
    return item;
  }
}
