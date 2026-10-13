/**
 * 新用户资料完善提示。
 * 进入首页时调用：若当前登录用户未填写昵称/姓名（新注册用户），
 * 弹出提示引导前往个人中心完善资料。
 *
 * 用法：在组件的 created/mounted 中调用
 *   import { promptProfileCompletion } from '@/utils/profileTip'
 *   promptProfileCompletion(this)
 */
export function promptProfileCompletion(vm) {
  try {
    const userStr = sessionStorage.getItem('user');
    if (!userStr) return;
    const user = JSON.parse(userStr);

    // 昵称与姓名都为空 → 判定为未完善资料的新用户
    const hasName = (user.nickname && String(user.nickname).trim()) ||
                    (user.realName && String(user.realName).trim());
    if (hasName) return;

    // 跳转目标：HR 角色进 /HrPerson，求职者进 /PerSon
    const target = Number(user.role) === 1 ? '/HrPerson' : '/PerSon';

    vm.$confirm(
      '欢迎新用户！为了帮助 HR 更好地了解你，请先完善个人资料（昵称/姓名、联系方式等）。',
      '完善个人资料',
      {
        confirmButtonText: '立即完善',
        cancelButtonText: '稍后再说',
        type: 'warning',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    ).then(() => {
      vm.$router.push(target);
    }).catch(() => {
      // 用户选择稍后再说，不强制
    });
  } catch (e) {
    console.error('资料完善提示失败:', e);
  }
}
