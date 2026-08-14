import assert from 'node:assert/strict';
import test from 'node:test';
import {
  recommendationReasonTexts,
  removeStudentRecommendation,
} from './studentRecommendation.js';

test('ưu tiên major rồi faculty và không hiển thị raw score', () => {
  const recommendation = {
    matchScore: 100,
    matchReasons: ['COMMON_INTERESTS', 'SAME_SCHOOL', 'SAME_MAJOR', 'SAME_FACULTY'],
    commonInterestCount: 3,
    academic: {
      school: { name: 'STU' },
      faculty: { name: 'Công nghệ Thông tin' },
      major: { name: 'Kỹ thuật phần mềm' },
    },
  };

  assert.deepEqual(recommendationReasonTexts(recommendation), [
    'Cùng ngành Kỹ thuật phần mềm',
    'Cùng khoa Công nghệ Thông tin',
  ]);
  assert.equal(recommendationReasonTexts(recommendation).join(' ').includes('100'), false);
});

test('dùng count cho interests và mutual connections', () => {
  const recommendation = {
    matchReasons: ['MUTUAL_CONNECTIONS', 'COMMON_INTERESTS'],
    commonInterestCount: 4,
    mutualConnectionCount: 2,
  };

  assert.deepEqual(recommendationReasonTexts(recommendation), [
    '4 sở thích chung',
    '2 kết nối chung',
  ]);
});

test('follow thành công chỉ loại đúng candidate', () => {
  assert.deepEqual(
    removeStudentRecommendation([{ userId: 1 }, { userId: 2 }], '1'),
    [{ userId: 2 }],
  );
});
