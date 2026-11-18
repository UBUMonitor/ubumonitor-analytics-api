package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.ItemnameApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.RangeApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.TableDataItemApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MoodleGradesAdapterTableResponseMapperTest {

  private final MoodleGradesAdapterTableResponseMapper mapper =
      Mappers.getMapper(MoodleGradesAdapterTableResponseMapper.class);

  @Test
  void synthesizesMissingCategoryFromMoodleTableAndLinksItsChildren() {
    GradeItem course = gradeItem(370L, "course", 85L, null);
    GradeItem manualGrade = gradeItem(390L, "manual", null, 92L);
    GradeItem moduleGrade = gradeItem(391L, "mod", null, 92L);
    List<GradeItem> allGradeItems = new ArrayList<>(List.of(course, manualGrade, moduleGrade));
    List<GradeItem> itemsWithoutParent = List.of(manualGrade, moduleGrade);

    GetGradesTableResponseApi response = new GetGradesTableResponseApi();
    TableApi table = new TableApi();
    table.setTabledata(
        List.of(
            categoryRow(
                "cat_92_13",
                "<div class=\"category-content\"><span>Ungraded (Attendance)</span></div>",
                List.of(85L)),
            gradeRow("row_390_13", List.of(85L, 92L)),
            gradeRow("row_391_13", List.of(85L, 92L))));
    response.setTables(List.of(table));

    mapper.fillParent(response, itemsWithoutParent, allGradeItems);

    GradeItem category =
        allGradeItems.stream().filter(item -> item.getId().equals(-92L)).findFirst().orElseThrow();
    assertEquals("category", category.getItemType());
    assertEquals(92L, category.getItemInstance());
    assertEquals(92L, category.getCategoryId());
    assertEquals(85L, category.getParentCategoryId());
    assertEquals(370L, category.getParentId());
    assertEquals("Ungraded (Attendance)", category.getName());
    assertNull(category.getGradeMin());
    assertNull(category.getGradeMax());

    assertEquals(-92L, manualGrade.getParentId());
    assertEquals(92L, manualGrade.getParentCategoryId());
    assertNull(manualGrade.getCategoryId());
    assertEquals(-92L, moduleGrade.getParentId());
    assertEquals(92L, moduleGrade.getParentCategoryId());
    assertNull(moduleGrade.getCategoryId());
  }

  @Test
  void mapsNumericRangeCellsOnlyToItemsWithoutExistingGradeBounds() {
    GradeItem gradeItem = gradeItem(399L, "mod", null, 85L);
    GradeItem itemWithExistingBounds = gradeItem(400L, "mod", null, 85L);
    itemWithExistingBounds.setGradeMin(BigDecimal.ZERO);
    itemWithExistingBounds.setGradeMax(BigDecimal.valueOf(20));
    GradeItem scaleItem = gradeItem(401L, "manual", null, 85L);
    List<GradeItem> allGradeItems =
        new ArrayList<>(List.of(gradeItem, itemWithExistingBounds, scaleItem));

    GetGradesTableResponseApi response = new GetGradesTableResponseApi();
    TableApi table = new TableApi();
    table.setTabledata(
        List.of(
            gradeRow("row_399_13", List.of(85L), "1.25&ndash;10.5"),
            gradeRow("row_400_13", List.of(85L), "0&ndash;100"),
            gradeRow("row_401_13", List.of(85L), "Absent&ndash;Present")));
    response.setTables(List.of(table));

    mapper.fillParent(
        response, List.of(gradeItem, itemWithExistingBounds, scaleItem), allGradeItems);

    assertEquals(new BigDecimal("1.25"), gradeItem.getGradeMin());
    assertEquals(new BigDecimal("10.5"), gradeItem.getGradeMax());
    assertEquals(BigDecimal.ZERO, itemWithExistingBounds.getGradeMin());
    assertEquals(BigDecimal.valueOf(20), itemWithExistingBounds.getGradeMax());
    assertNull(scaleItem.getGradeMin());
    assertNull(scaleItem.getGradeMax());
  }

  private GradeItem gradeItem(Long id, String type, Long instance, Long parentCategoryId) {
    GradeItem item = new GradeItem();
    item.setId(id);
    item.setItemType(type);
    item.setItemInstance(instance);
    item.setParentCategoryId(parentCategoryId);
    return item;
  }

  private TableDataItemApi categoryRow(String id, String content, List<Long> parentCategories) {
    TableDataItemApi row = new TableDataItemApi();
    ItemnameApi itemname = new ItemnameApi();
    itemname.setId(id);
    itemname.setContent(content);
    row.setItemname(itemname);
    row.setParentcategories(parentCategories);
    return row;
  }

  private TableDataItemApi gradeRow(String id, List<Long> parentCategories) {
    return gradeRow(id, parentCategories, null);
  }

  private TableDataItemApi gradeRow(String id, List<Long> parentCategories, String rangeContent) {
    TableDataItemApi row = new TableDataItemApi();
    ItemnameApi itemname = new ItemnameApi();
    itemname.setId(id);
    row.setItemname(itemname);
    row.setParentcategories(parentCategories);
    if (rangeContent != null) {
      RangeApi range = new RangeApi();
      range.setContent(rangeContent);
      row.setRange(range);
    }
    return row;
  }
}
