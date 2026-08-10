package com.stu.edu.vn.backend.academic.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.academic.service.AcademicCatalogService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Kiểm chứng path và response envelope của read-only Academic master API. */
class AcademicCatalogControllerTest {
    private final AcademicCatalogService service = org.mockito.Mockito.mock(AcademicCatalogService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AcademicCatalogController(service)).build();
    }

    @Test
    void schoolAutocompletePassesKeywordAndLimit() throws Exception {
        when(service.searchSchools("Công", 5)).thenReturn(List.of(
                new SchoolResponse(1L, "Trường Đại học Công Nghệ Sài Gòn", "STU")
        ));

        mockMvc.perform(get("/api/v1/academic/schools").param("keyword", "Công").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].shortName").value("STU"));
        verify(service).searchSchools("Công", 5);
    }

    @Test
    void facultyAndMajorEndpointsKeepParentId() throws Exception {
        when(service.searchFaculties(1L, null, null)).thenReturn(List.of(new AcademicItemResponse(2L, "CNTT")));
        when(service.searchMajors(2L, null, null)).thenReturn(List.of(new AcademicItemResponse(3L, "CNTT")));

        mockMvc.perform(get("/api/v1/academic/schools/1/faculties"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(2));
        mockMvc.perform(get("/api/v1/academic/faculties/2/majors"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(3));
    }

    @Test
    void interestsReturnsActiveCatalogProjection() throws Exception {
        when(service.getInterests()).thenReturn(List.of(new InterestResponse(1L, "Lập trình")));

        mockMvc.perform(get("/api/v1/interests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Lập trình"));
    }
}
