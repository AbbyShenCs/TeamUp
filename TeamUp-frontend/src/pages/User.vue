<template>
  <template v-if="user">
    <van-cell to="/user/update">
      <van-card
          :desc="`${user.profile == null ? '写个简介介绍一下自己！' : user.profile}`"
          :thumb="user.avatarUrl"
          :title="`${user.username == null ? '😀：给自己来个昵称' : user.username}`"
      >
        <template #tags>
          <van-tag plain type="danger" v-for="tag in user.tags" style="margin-right: 8px; margin-top: 8px">
            {{ tag }}
          </van-tag>
        </template>
      </van-card>
    </van-cell>
    <van-divider/>

    <van-cell center is-link title="个人标签" to="/user/updateTags">
      <template #icon>
        <van-icon name="label-o"  size="18"/>
      </template>
    </van-cell>

    <van-divider/>
    <van-cell center is-link title="我创建的队伍" to="/user/team/create">
      <template #icon>
        <van-icon name="cluster-o" size="18"/>
      </template>
    </van-cell>
    <van-divider/>
    <van-cell center is-link title="我加入的队伍" to="/user/team/join">
      <template #icon>
        <van-icon name="friends-o" size="18"/>
      </template>
    </van-cell>

    <van-divider/>
    <van-cell center title="退出登录" @click="quit">
      <template #icon>
        <van-icon name="close" size="18"/>
      </template>
    </van-cell>
  </template>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user";
import {showSuccessToast} from "vant";
import myAxios from "../plugins/myAxios";

const user = ref();

onMounted(async () => {
  user.value = await getCurrentUser();
  if (user.value.tags) {
    user.value.tags = JSON.parse(user.value.tags);
  }
})

const router = useRouter();
const quit = async () => {
  const res = await myAxios.post("/user/logout");
  if (res.code === 0) {
    showSuccessToast("退出成功");
    await router.push({
      path: '/user/login',
    })
  }
}

</script>

<style scoped>
:root {
  --van-card-font-size: 10px;
}
</style>
