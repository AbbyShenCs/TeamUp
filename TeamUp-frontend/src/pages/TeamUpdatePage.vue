<template>
  <div id="teamUpdatePage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名"
            placeholder="请输入队伍名"
            :rules="[{ required: true, message: '请输入队伍名' }]"
        />
        <van-field
            v-model="addTeamData.description"
            rows="4"
            autosize
            label="队伍描述"
            type="textarea"
            placeholder="请输入队伍描述"
        />
        <van-field
            v-model="addTeamData.expireTime"
            is-link
            readonly
            name="calendar"
            label="日历"
            placeholder="点击选择日期"
            @click="showCalendar = true"
        />
        <van-calendar v-model:show="showCalendar" @confirm="onConfirm"/>
        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status"  direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
            v-if="Number(addTeamData.status) === 2"
            v-model="addTeamData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入队伍密码"
            :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">

import {useRoute, useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";
import {showFailToast, showSuccessToast} from "vant";
import {onMounted, ref} from "vue";

const router = useRouter();
const route = useRoute();

const showCalendar = ref(false);
const onConfirm = (date) => {
  //@ts-ignore
  addTeamData.value.expireTime = `${date.getFullYear()}-${date.getMonth() + 1 >= 10 ? date.getMonth() + 1 : '0' + (date.getMonth() + 1)}-${date.getDate() >= 10 ? date.getDate() + 1 : '0' + date.getDate()} 00:00:00`;
  showCalendar.value = false;
};

// 需要用户填写的表单数据
const addTeamData = ref({})

const id = route.query.id;

//获取之前队伍的信息
onMounted(async () => {
  if (id <= 0) {
    showFailToast("队伍加载失败");
    return;
  }
  const res = await myAxios.get("/team/get", {
    params: {
      id: id,
    }
  });
  console.log(res)
  if (res?.code === 0) {
    addTeamData.value = res.data;

    addTeamData.value.expireTime = new Date(addTeamData.value.expireTime).getFullYear() + '-' + (new Date(addTeamData.value.expireTime).getMonth() + 1) + '-' + (new Date(addTeamData.value.expireTime).getDate()) + ' 00:00:00'

  } else {
    showFailToast("队伍加载失败，请刷新重试");
  }
})

// 提交
const onSubmit = async () => {
  const postData = {
    ...addTeamData.value,
    status: Number(addTeamData.value.status)
  }
  // todo 前端参数校验
  const res = await myAxios.post("/team/update", postData);
  if (res?.code === 0 && res.data) {
    showSuccessToast('更新成功');
    await router.push({
      path: '/user/team/create',
      replace: true,
    });
  } else {
    showFailToast('更新失败');
  }
}
</script>

<style scoped>
#teamPage {
}
</style>
