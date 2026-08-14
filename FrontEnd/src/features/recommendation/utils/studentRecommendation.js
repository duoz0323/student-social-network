const REASON_PRIORITY = Object.freeze([
  'SAME_MAJOR',
  'SAME_FACULTY',
  'SAME_SCHOOL',
  'SAME_ENTRY_YEAR',
  'COMMON_INTERESTS',
  'MUTUAL_CONNECTIONS',
]);

function reasonText(reason, recommendation) {
  const academic = recommendation.academic ?? {};
  switch (reason) {
    case 'SAME_MAJOR':
      return academic.major?.name ? `Cùng ngành ${academic.major.name}` : 'Cùng ngành học';
    case 'SAME_FACULTY':
      return academic.faculty?.name ? `Cùng khoa ${academic.faculty.name}` : 'Cùng khoa';
    case 'SAME_SCHOOL':
      return academic.school?.name ? `Cùng trường ${academic.school.name}` : 'Cùng trường';
    case 'SAME_ENTRY_YEAR':
      return academic.entryYear ? `Cùng khóa ${academic.entryYear}` : 'Cùng năm nhập học';
    case 'COMMON_INTERESTS':
      return `${recommendation.commonInterestCount ?? 0} sở thích chung`;
    case 'MUTUAL_CONNECTIONS':
      return `${recommendation.mutualConnectionCount ?? 0} kết nối chung`;
    default:
      return null;
  }
}

/** Chọn tối đa hai lý do quan trọng theo thứ tự UX đã chốt, không dựa vào raw score. */
export function recommendationReasonTexts(recommendation, limit = 2) {
  const matches = new Set(recommendation.matchReasons ?? []);
  return REASON_PRIORITY
    .filter((reason) => matches.has(reason))
    .map((reason) => reasonText(reason, recommendation))
    .filter(Boolean)
    .slice(0, limit);
}

/** Follow thành công loại candidate theo ID mà không làm mất các card còn lại. */
export function removeStudentRecommendation(recommendations, userId) {
  return recommendations.filter((item) => String(item.userId) !== String(userId));
}
