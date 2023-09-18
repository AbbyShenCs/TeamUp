<template>
  <template v-if="teamData">
    <van-cell-group inset>
      <van-cell>
        <van-card
            :desc="teamData.description"
            :thumb="teamData.createUser.avatarUrl"
            :title="teamData?.name"
        >
          <template #tags>
            <van-tag plain style="margin-right: 8px; margin-top: 8px" type="danger">
              {{
                teamStatusEnum[teamData.status]
              }}
            </van-tag>
          </template>
        </van-card>
      </van-cell>
      <van-divider/>
      <van-cell :value="teamData.id" center title=" 队伍ID">
        <template #icon>
          <van-icon name="points" size="18"/>
        </template>
      </van-cell>
      <van-divider/>
      <van-cell :value="teamData.name" center title=" 队伍名">
        <template #icon>
          <van-icon name="award-o" size="18"/>
        </template>
      </van-cell>
      <van-divider/>
      <van-cell :value="teamData.createUser.username" center title=" 队长">
        <template #icon>
          <van-icon name="manager-o" size="18"/>
        </template>
      </van-cell>
      <van-divider/>
      <van-cell :value="teamData.description" center title=" 队伍描述">
        <template #icon>
          <van-icon name="label-o" size="18"/>
        </template>
      </van-cell>
      <van-divider/>
      <van-cell :value="teamData.hasJoinNum+' / '+teamData.maxNum" center title=" 队伍人数">
        <template #icon>
          <van-icon name="friends-o" size="18"/>
        </template>
      </van-cell>
      <van-divider/>
      <van-cell v-if="teamData.expireTime" center>
        <template #icon>
          <van-icon name="underway-o" size="18"/>
          {{
            '过期时间: ' + new Date(teamData.expireTime).getFullYear() + '-' + (new Date(teamData.expireTime).getMonth() + 1) + '-' + (new Date(teamData.expireTime).getDate()) + ' 00:00:00'
          }}
        </template>
      </van-cell>
      <van-divider/>
      <van-popover v-model:show="showPopover" placement="right-start">
        <van-grid
            :border="false"
            clickable
            column-num="3"
            square
            style="width: 240px;"
        >
          <van-grid-item
              v-for="operation in operations"
              :icon="operation.icon"
              :text="operation.text"
              @click="doClickEvent(operation.clickEvent)"
          />
        </van-grid>
        <template #reference>
          <van-cell center title=" 操作">
            <template #icon>
              <van-icon name="info-o" size="18"/>
            </template>
          </van-cell>
        </template>
      </van-popover>
      <van-divider/>
      <van-cell center title=" 已加入的用户">
        <template #icon>
          <van-icon name="label-o" size="18"/>
        </template>
      </van-cell>
      <UserCardList :user-list="teamData.joinUsers" :loading="loading" />
    </van-cell-group>
  </template>
  <van-dialog v-model:show="showPasswordDialog" show-cancel-button title="请输入密码" @cancel="doJoinCancel"
              @confirm="doJoinTeam">
    <van-field v-model="password" placeholder="请输入密码" type="password"/>
  </van-dialog>
</template>

<script lang="ts" setup>

// 获取之前的队伍信息
import {onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";
import {teamStatusEnum} from "../constants/team";
import UserCardList from "../components/UserCardList.vue";
import {TeamType} from "../models/team";
import {getCurrentUser} from "../services/user";
import {showFailToast, showSuccessToast} from "vant";

const route = useRoute();
const router = useRouter();
const teamData = ref();
const showPasswordDialog = ref(false);
const password = ref('');
const currentUser = ref();
const joinTeamId = ref();
const showPopover = ref(false);
const operations = ref([]);

let loading = ref(true);

const id = route.query.id;
onMounted(async () => {
      loading.value = true;
      currentUser.value = await getCurrentUser();
      if (id <= 0) {
        showFailToast('加载队伍失败');
        return;
      }
      const res = await myAxios.get("/team/get", {
        params: {
          id,
        }
      });
      if (res?.code === 0) {
        teamData.value = res.data;
      } else {
        showFailToast('加载队伍失败' + res.description);
        await router.push({
          path: '/team',
        })
      }

      if (teamData.value.userId === currentUser.value?.id) {
        operations.value.push({text: '更新队伍', clickEvent: "updateTeam", icon: "add-o"});
      }

      if (!teamData.value.hasJoin) {
        operations.value.push({text: '加入队伍', clickEvent: "joinTeam", icon: "contact"});
      } else {
        operations.value.push({text: '退出队伍', clickEvent: "quitTeam", icon: "close"});
      }

      if (teamData.value.userId === currentUser.value?.id) {
        operations.value.push({text: '解散队伍', clickEvent: "deleteTeam", icon: "delete"});
      }
      loading.value = false;
    }
)

const doClickEvent = (operationName: string) => {
  if (operationName === "joinTeam") {
    preJoinTeam(teamData.value);
  }
  if (operationName === "quitTeam") {
    doQuitTeam(teamData.value.id);
  }
  if (operationName === "updateTeam") {
    doUpdateTeam(teamData.value.id);
  }
  if (operationName === "deleteTeam") {
    doDeleteTeam(teamData.value.id);
  }
}

const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}


const doJoinTeam = async () => {
  if (!joinTeamId.value) {
    return;
  }
  const res = await myAxios.post('/team/join', {
    teamId: joinTeamId.value,
    password: password.value
  });
  if (res?.code === 0) {
    showSuccessToast('加入成功');
    doJoinCancel();

  } else {
    showFailToast('加入失败' + (res.description ? `，${res.description}` : ''));
    doJoinCancel();
  }
}
const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 跳转至更新队伍页
 * @param id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id,
    }
  })
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 0) {
    showSuccessToast('退出成功');
    await router.push({
      path: '/team',
      replace: true,
    });
  } else {
    showFailToast('退出失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/delete', {
    id,
  });
  if (res?.code === 0) {
    showSuccessToast('退出成功');
    await router.push({
      path: '/team',
      replace: true,
    });
  } else {
    showFailToast('退出失败' + (res.description ? `，${res.description}` : ''));
  }
}
</script>

<style scoped>

</style>
