package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.admin.dto.response.AdminFacultyResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminInterestResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminMajorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminSchoolResponse;
import com.stu.edu.vn.backend.admin.service.AdminAcademicService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Kiểm chứng path, pagination và payload V1 của Admin Academic API. */
class AdminAcademicControllerTest {
    private final AdminAcademicService service = org.mockito.Mockito.mock(AdminAcademicService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminAcademicController(service)).build();
    }

    @Test
    void listsAndCreatesSchool() throws Exception {
        AdminSchoolResponse school = school(1L, "Đại học Công nghệ Sài Gòn", "STU", AcademicStatus.ACTIVE);
        when(service.getSchools("Công nghệ", 0, 20))
                .thenReturn(new PageResponse<>(List.of(school), 0, 20, 1, 1, true, true));
        when(service.createSchool("Đại học Công nghệ Sài Gòn", "STU")).thenReturn(school);

        mockMvc.perform(get("/api/v1/admin/academic/schools").param("keyword", "Công nghệ"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].shortName").value("STU"));
        mockMvc.perform(post("/api/v1/admin/academic/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Đại học Công nghệ Sài Gòn\",\"shortName\":\"STU\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void updatesSchoolAndChangesStatus() throws Exception {
        when(service.updateSchool(1L, "Tên mới", "TN"))
                .thenReturn(school(1L, "Tên mới", "TN", AcademicStatus.ACTIVE));
        when(service.changeSchoolStatus(1L, AcademicStatus.INACTIVE))
                .thenReturn(school(1L, "Tên mới", "TN", AcademicStatus.INACTIVE));

        mockMvc.perform(put("/api/v1/admin/academic/schools/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Tên mới\",\"shortName\":\"TN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Tên mới"));
        mockMvc.perform(patch("/api/v1/admin/academic/schools/1/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void exposesFacultyMajorAndInterestHierarchy() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 8, 10, 10, 0);
        when(service.createFaculty(1L, "Công nghệ thông tin"))
                .thenReturn(new AdminFacultyResponse(2L, 1L, "Công nghệ thông tin", AcademicStatus.ACTIVE, now, now));
        when(service.createMajor(2L, "Kỹ thuật phần mềm"))
                .thenReturn(new AdminMajorResponse(3L, 2L, "Kỹ thuật phần mềm", AcademicStatus.ACTIVE, now, now));
        when(service.createInterest("Lập trình"))
                .thenReturn(new AdminInterestResponse(4L, "Lập trình", AcademicStatus.ACTIVE, now, now));

        mockMvc.perform(post("/api/v1/admin/academic/schools/1/faculties")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Công nghệ thông tin\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.schoolId").value(1));
        mockMvc.perform(post("/api/v1/admin/academic/faculties/2/majors")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Kỹ thuật phần mềm\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.facultyId").value(2));
        mockMvc.perform(post("/api/v1/admin/academic/interests")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Lập trình\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.id").value(4));
        verify(service).createFaculty(1L, "Công nghệ thông tin");
        verify(service).createMajor(2L, "Kỹ thuật phần mềm");
        verify(service).createInterest("Lập trình");
    }

    private AdminSchoolResponse school(Long id, String name, String shortName, AcademicStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 8, 10, 10, 0);
        return new AdminSchoolResponse(id, name, shortName, status, now, now);
    }
}
