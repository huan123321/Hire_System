/**
 * 公司默认头像工具：公司无 logo 时，按行业主题色 + 公司名首字生成默认头像。
 * 使用内联 SVG data URI，无需额外图片资源。
 */

// 行业 → 渐变色（起始色, 结束色）
const INDUSTRY_COLORS = {
  '互联网': ['#409EFF', '#66B1FF'],
  '电子商务': ['#FF8A00', '#FFB25C'],
  '金融': ['#E6A23C', '#F0C060'],
  '教育': ['#67C23A', '#95D475'],
  '通信': ['#00C2A1', '#4DD0C0'],
  '新能源汽车': ['#3A7BD5', '#5C9DF0'],
  '交通出行': ['#7C6FF0', '#A49DF5'],
  '旅游': ['#2BC4A8', '#6ED9C5'],
  '人工智能': ['#5B6BFF', '#8C98FF'],
  '医疗': ['#F56C6C', '#FF9FB2'],
  '房地产': ['#B88230', '#D4A75B'],
  '游戏': ['#7C4DFF', '#A98BFF']
};

const DEFAULT_COLORS = ['#909399', '#B0B6BF'];

// 行业关键词 → 颜色（模糊匹配）
function industryColors(industry) {
  if (!industry) return DEFAULT_COLORS;
  const kw = String(industry);
  if (INDUSTRY_COLORS[kw]) return INDUSTRY_COLORS[kw];
  for (const key of Object.keys(INDUSTRY_COLORS)) {
    if (kw.includes(key) || key.includes(kw)) {
      return INDUSTRY_COLORS[key];
    }
  }
  return DEFAULT_COLORS;
}

// 取公司名首字（中文/英文首字母）
function companyInitial(name) {
  if (!name) return '企';
  const s = String(name).trim();
  if (!s) return '企';
  const code = s.charCodeAt(0);
  // 中文或常规字符
  if ((code >= 0x4E00 && code <= 0x9FFF) || /[A-Za-z0-9]/.test(s[0])) {
    return s[0].toUpperCase();
  }
  return '企';
}

/**
 * 按公司信息生成默认头像 SVG data URI
 * @param {object} company 公司对象（name、industry 字段）
 */
export function defaultCompanyLogo(company) {
  const colors = industryColors(company && company.industry);
  const initial = companyInitial(company && company.name);
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">' +
    '<defs><linearGradient id="clg" x1="0" y1="0" x2="1" y2="1">' +
    `<stop offset="0%" stop-color="${colors[0]}"/><stop offset="100%" stop-color="${colors[1]}"/>` +
    '</linearGradient></defs>' +
    '<rect width="100" height="100" rx="16" fill="url(#clg)"/>' +
    `<text x="50" y="64" font-size="44" font-weight="bold" fill="#ffffff" text-anchor="middle" font-family="'Microsoft YaHei','PingFang SC',sans-serif">${initial}</text>` +
    '</svg>';
  return 'data:image/svg+xml;utf8,' + encodeURIComponent(svg);
}

/**
 * 获取公司最终头像：优先公司 logo，其次按行业/名称生成默认头像
 * @param {object} company 公司对象（logo、name、industry 字段）
 */
export function getCompanyLogo(company) {
  if (company && company.logo) return company.logo;
  return defaultCompanyLogo(company);
}
