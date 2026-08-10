import test from 'node:test';
import assert from 'node:assert/strict';
import { ACADEMIC_ENDPOINTS } from '../src/api/apiEndpoints.js';
import {
  buildProfileUpdatePayload,
  createAcademicSelection,
  createEntryYears,
  mapAcademicProfileError,
  selectFaculty,
  selectSchool,
  shouldSearchAcademic,
  toggleInterest,
} from '../src/features/profile/utils/academicProfileUtils.js';

test('Academic master endpoints encode đúng parent ID cho School, Faculty và Major search', () => {
  assert.equal(ACADEMIC_ENDPOINTS.schools, '/api/v1/academic/schools');
  assert.equal(ACADEMIC_ENDPOINTS.faculties('stu/01'), '/api/v1/academic/schools/stu%2F01/faculties');
  assert.equal(ACADEMIC_ENDPOINTS.majors('fit 01'), '/api/v1/academic/faculties/fit%2001/majors');
  assert.equal(ACADEMIC_ENDPOINTS.interests, '/api/v1/interests');
});

test('autocomplete không search input rỗng và khóa Faculty/Major khi thiếu parent', () => {
  assert.equal(shouldSearchAcademic({ kind: 'school', keyword: 'STU', searchActive: true }), true);
  assert.equal(shouldSearchAcademic({ kind: 'school', keyword: '   ', searchActive: true }), false);
  assert.equal(shouldSearchAcademic({ kind: 'faculty', keyword: 'CNTT', searchActive: true }), false);
  assert.equal(shouldSearchAcademic({ kind: 'faculty', parentId: 1, keyword: 'CNTT', searchActive: true }), true);
  assert.equal(shouldSearchAcademic({ kind: 'major', parentId: 2, keyword: 'IT', searchActive: true, disabled: true }), false);
});

test('đổi School xóa Faculty và Major thuộc hierarchy cũ', () => {
  const current = {
    school: { id: 1, name: 'STU' },
    faculty: { id: 2, name: 'Khoa CNTT' },
    major: { id: 3, name: 'CNTT' },
    entryYear: 2022,
  };
  assert.deepEqual(selectSchool(current, { id: 4, name: 'Trường khác' }), {
    school: { id: 4, name: 'Trường khác' },
    faculty: null,
    major: null,
    entryYear: 2022,
  });
});

test('đổi Faculty xóa Major nhưng giữ School và Entry Year', () => {
  const current = {
    school: { id: 1, name: 'STU' },
    faculty: { id: 2, name: 'Khoa cũ' },
    major: { id: 3, name: 'Ngành cũ' },
    entryYear: 2023,
  };
  assert.deepEqual(selectFaculty(current, { id: 5, name: 'Khoa mới' }), {
    school: current.school,
    faculty: { id: 5, name: 'Khoa mới' },
    major: null,
    entryYear: 2023,
  });
});

test('Entry Year sinh động từ năm hiện tại về 1900', () => {
  const years = createEntryYears(2026);
  assert.equal(years[0], 2026);
  assert.equal(years.at(-1), 1900);
  assert.equal(years.length, 127);
});

test('Interests không duplicate, khóa lựa chọn thứ 11 và vẫn cho bỏ chọn', () => {
  const selected = Array.from({ length: 10 }, (_, index) => index + 1);
  assert.deepEqual(toggleInterest([1, 1, 2], 2).interestIds, [1]);

  const blocked = toggleInterest(selected, 11);
  assert.equal(blocked.limitReached, true);
  assert.deepEqual(blocked.interestIds, selected);

  const removed = toggleInterest(selected, 4);
  assert.equal(removed.limitReached, false);
  assert.equal(removed.interestIds.includes(4), false);
});

test('profile prefill giữ object Academic và legacy profile null vẫn hợp lệ', () => {
  const profile = {
    school: { id: 1, name: 'STU' },
    faculty: { id: 2, name: 'Khoa CNTT' },
    major: { id: 3, name: 'CNTT' },
    entryYear: 2022,
  };
  assert.deepEqual(createAcademicSelection(profile), profile);
  assert.deepEqual(createAcademicSelection({}), {
    school: null, faculty: null, major: null, entryYear: null,
  });
});

test('payload save chỉ gửi ID và skip không tự thêm Academic hoặc Interests rỗng', () => {
  const basic = { displayName: '  Minh  ', dateOfBirth: '2000-01-01', bio: '  Xin chào  ' };
  const academic = {
    school: { id: 1, name: 'Không được gửi tên trường' },
    faculty: { id: 2, name: 'Không được gửi tên khoa' },
    major: { id: 3, name: 'Không được gửi tên ngành' },
    entryYear: 2022,
  };

  assert.deepEqual(buildProfileUpdatePayload({ basic, academic }), {
    displayName: 'Minh', dateOfBirth: '2000-01-01', bio: 'Xin chào',
  });
  assert.deepEqual(buildProfileUpdatePayload({
    basic, academic, interestIds: [1, 1, 3], includeAcademic: true, includeInterests: true,
  }), {
    displayName: 'Minh',
    dateOfBirth: '2000-01-01',
    bio: 'Xin chào',
    academic: { schoolId: 1, facultyId: 2, majorId: 3, entryYear: 2022 },
    interestIds: [1, 3],
  });
});

test('business errors Academic được ánh xạ thành thông báo hành động được', () => {
  assert.match(mapAcademicProfileError({ code: 'ACADEMIC_FACULTY_SCHOOL_MISMATCH' }), /không thuộc trường/);
  assert.match(mapAcademicProfileError({ code: 'ACADEMIC_MAJOR_FACULTY_MISMATCH' }), /không thuộc khoa/);
  assert.match(mapAcademicProfileError({ code: 'ACADEMIC_ENTRY_YEAR_INVALID' }), /1900/);
  assert.match(mapAcademicProfileError({ code: 'INTEREST_LIMIT_EXCEEDED' }), /10/);
});
