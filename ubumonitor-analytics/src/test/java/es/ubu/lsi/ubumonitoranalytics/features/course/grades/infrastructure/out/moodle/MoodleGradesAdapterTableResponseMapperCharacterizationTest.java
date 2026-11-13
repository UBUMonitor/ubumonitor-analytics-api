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

class MoodleGradesAdapterTableResponseMapperCharacterizationTest {

  private final MoodleGradesAdapterTableResponseMapper mapper =
      Mappers.getMapper(MoodleGradesAdapterTableResponseMapper.class);

  @Test
  void acceptsAnEmptyResponseWithoutChangingItems() {
    GradeItem item = gradeItem(5L, "manual", null);
    List<GradeItem> allGradeItems = new ArrayList<>(List.of(item));

    mapper.fillParent(null, List.of(item), allGradeItems);

    assertEquals(List.of(item), allGradeItems);
    assertNull(item.getParentId());
  }

  @Test
  void usesTheFirstRangeForDuplicateGradeItemRowsAndIds() {
    GradeItem first = gradeItem(7L, "manual", null);
    GradeItem duplicate = gradeItem(7L, "manual", null);
    List<GradeItem> allGradeItems = new ArrayList<>(List.of(first, duplicate));
    GetGradesTableResponseApi response =
        response(gradeRow("row_7_13", "0–10"), gradeRow("row_7_13", "0–100"));

    mapper.fillParent(response, List.of(), allGradeItems);

    assertEquals(new BigDecimal("0"), first.getGradeMin());
    assertEquals(new BigDecimal("10"), first.getGradeMax());
    assertNull(duplicate.getGradeMin());
    assertNull(duplicate.getGradeMax());
  }

  @Test
  void leavesAnItemUnparentedWhenItsCategoryHasNoTableRowOrContainer() {
    GradeItem item = gradeItem(12L, "manual", 999L);
    List<GradeItem> allGradeItems = new ArrayList<>(List.of(item));
    GetGradesTableResponseApi response = response(gradeRow("row_12_13", null));

    mapper.fillParent(response, List.of(item), allGradeItems);

    assertNull(item.getParentId());
    assertEquals(List.of(item), allGradeItems);
  }

  @Test
  void ignoresAnInvalidGradeRange() {
    GradeItem item = gradeItem(8L, "manual", null);
    GetGradesTableResponseApi response = response(gradeRow("row_8_13", "not a range"));

    mapper.fillParent(response, List.of(), new ArrayList<>(List.of(item)));

    assertNull(item.getGradeMin());
    assertNull(item.getGradeMax());
  }

  private GetGradesTableResponseApi response(TableDataItemApi... rows) {
    TableApi table = new TableApi();
    table.setTabledata(List.of(rows));
    GetGradesTableResponseApi response = new GetGradesTableResponseApi();
    response.setTables(List.of(table));
    return response;
  }

  private TableDataItemApi gradeRow(String id, String rangeContent) {
    TableDataItemApi row = new TableDataItemApi();
    ItemnameApi itemname = new ItemnameApi();
    itemname.setId(id);
    row.setItemname(itemname);
    if (rangeContent != null) {
      RangeApi range = new RangeApi();
      range.setContent(rangeContent);
      row.setRange(range);
    }
    return row;
  }

  private GradeItem gradeItem(Long id, String type, Long parentCategoryId) {
    GradeItem item = new GradeItem();
    item.setId(id);
    item.setItemType(type);
    item.setParentCategoryId(parentCategoryId);
    return item;
  }
}
