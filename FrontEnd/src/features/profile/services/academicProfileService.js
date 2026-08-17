import { ACADEMIC_ENDPOINTS } from '../../../api/apiEndpoints.js';
import { httpClient } from '../../../api/httpClient.js';
import { compactParams, requestData } from '../../../api/requestData.js';

// Lấy mức tối đa API cho phép để lần mở đầu hiển thị nhiều master data hiện có nhất.
const AUTOCOMPLETE_LIMIT = 20;

// Master data luôn được đọc từ Backend để Frontend không hard-code tên trường, khoa hoặc ngành.
export const academicProfileService = Object.freeze({
  searchSchools(keyword, signal) {
    return requestData(httpClient.get(ACADEMIC_ENDPOINTS.schools, {
      params: compactParams({ keyword: keyword.trim(), limit: AUTOCOMPLETE_LIMIT }),
      signal,
    }));
  },

  searchFaculties(schoolId, keyword, signal) {
    return requestData(httpClient.get(ACADEMIC_ENDPOINTS.faculties(schoolId), {
      params: compactParams({ keyword: keyword.trim(), limit: AUTOCOMPLETE_LIMIT }),
      signal,
    }));
  },

  searchMajors(facultyId, keyword, signal) {
    return requestData(httpClient.get(ACADEMIC_ENDPOINTS.majors(facultyId), {
      params: compactParams({ keyword: keyword.trim(), limit: AUTOCOMPLETE_LIMIT }),
      signal,
    }));
  },

  getInterests(signal) {
    return requestData(httpClient.get(ACADEMIC_ENDPOINTS.interests, { signal }));
  },
});
