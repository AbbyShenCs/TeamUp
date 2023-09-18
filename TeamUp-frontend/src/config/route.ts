import Team from '../pages/Team.vue';
import Index from '../pages/Index.vue';
import User from '../pages/User.vue';
import UserUpdatePage from '../pages/UserUpdatePage.vue';
import SearchPage from '../pages/SearchPage.vue';
import EditUserPage from '../pages/EditUserPage.vue';
import UserResultPage from '../pages/UserResultPage.vue';
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserTeamCreate from "../pages/UserTeamCreate.vue";
import UserTeamJoin from "../pages/UserTeamJoin.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import UserRegisterPage from "../pages/UserRegisterPage.vue";
import TeamDetailPage from "../pages/TeamDetailPage.vue";
import UpdateTagsPages from "../pages/UpdateTagsPages.vue";

// 定义路由
const routes = [
    { path: '/', component: Index },
    { path: '/team', title: '找队伍', component: Team },
    { path: '/user', title: '主页', component: User },
    { path: '/user/update', title: '用户详情', component: UserUpdatePage },
    { path: '/searchPage', title: '找伙伴', component: SearchPage },
    { path: '/user/edit', title: '编辑信息', component: EditUserPage },
    { path: '/user/list', title: '用户列表', component: UserResultPage },
    { path: '/user/login', title: '登录', component: UserLoginPage },
    { path: '/user/register', title: '注册', component: UserRegisterPage },
    { path: '/team/add', title: '创建队伍', component: TeamAddPage },
    { path: '/team/update', title: '更新队伍', component: TeamUpdatePage },
    {path: '/team/detail', title: '队伍详情', component: TeamDetailPage},
    { path: '/user/team/join', title: '我加入的队伍', component: UserTeamJoin },
    { path: '/user/team/create', title: '我创建的队伍', component: UserTeamCreate },
    { path: '/user/updateTags', title: '选择标签', component: UpdateTagsPages },

    {
        path: "/:pathMatch(.*)*", name: "notFound", component: Index,  // 引入 组件
    },

]
export default routes
