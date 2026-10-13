/**
 * 默认头像工具：用户未上传头像时，按性别显示男/女默认头像。
 * 使用内联 SVG data URI，无需额外图片资源。
 */

// 男性默认头像：蓝色系 + 白色人形剪影
const MALE_AVATAR = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">' +
  '<defs><linearGradient id="mg" x1="0" y1="0" x2="1" y2="1">' +
  '<stop offset="0%" stop-color="#409EFF"/><stop offset="100%" stop-color="#5BC0F0"/></linearGradient></defs>' +
  '<rect width="100" height="100" rx="50" fill="url(#mg)"/>' +
  '<circle cx="50" cy="36" r="15" fill="#ffffff"/>' +
  '<path d="M50 56c-15 0-25 9-25 23h50c0-14-10-23-25-23z" fill="#ffffff"/>' +
  '<text x="81" y="86" font-size="22" font-weight="bold" fill="#ffffff" text-anchor="middle">♂</text>' +
  '</svg>'
);

// 女性默认头像：粉色系 + 白色人形剪影
const FEMALE_AVATAR = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">' +
  '<defs><linearGradient id="fg" x1="0" y1="0" x2="1" y2="1">' +
  '<stop offset="0%" stop-color="#F56C6C"/><stop offset="100%" stop-color="#FF9FB2"/></linearGradient></defs>' +
  '<rect width="100" height="100" rx="50" fill="url(#fg)"/>' +
  '<circle cx="50" cy="36" r="15" fill="#ffffff"/>' +
  '<path d="M50 56c-15 0-25 9-25 23h50c0-14-10-23-25-23z" fill="#ffffff"/>' +
  '<text x="81" y="86" font-size="22" font-weight="bold" fill="#ffffff" text-anchor="middle">♀</text>' +
  '</svg>'
);

// 未知性别默认头像：灰色系
const UNKNOWN_AVATAR = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">' +
  '<defs><linearGradient id="ug" x1="0" y1="0" x2="1" y2="1">' +
  '<stop offset="0%" stop-color="#A0A8B8"/><stop offset="100%" stop-color="#C8CFDA"/></linearGradient></defs>' +
  '<rect width="100" height="100" rx="50" fill="url(#ug)"/>' +
  '<circle cx="50" cy="36" r="15" fill="#ffffff"/>' +
  '<path d="M50 56c-15 0-25 9-25 23h50c0-14-10-23-25-23z" fill="#ffffff"/>' +
  '</svg>'
);

/**
 * 归一化性别值：
 *   true / 'male' / '男' / 1      → 男
 *   false / 'female' / '女' / 0   → 女
 *   其他                          → 未知
 */
function normalizeGender(gender) {
  if (gender === true || gender === 1 || gender === 'male' || gender === '男' || gender === 'Male') return 'male';
  if (gender === false || gender === 0 || gender === 'female' || gender === '女' || gender === 'Female') return 'female';
  return '';
}

/**
 * 根据性别返回默认头像 data URI。
 * @param {*} gender 性别：Boolean / 'male' / 'female' / '男' / '女' 等
 */
export function defaultAvatar(gender) {
  const g = normalizeGender(gender);
  if (g === 'male') return MALE_AVATAR;
  if (g === 'female') return FEMALE_AVATAR;
  return UNKNOWN_AVATAR;
}

/**
 * 获取用户最终头像：优先用户自定义头像，其次按性别显示默认头像。
 * @param {object} user 用户对象（含 avatar、gender 字段）
 */
export function getUserAvatar(user) {
  if (user && user.avatar) return user.avatar;
  return defaultAvatar(user && user.gender);
}
