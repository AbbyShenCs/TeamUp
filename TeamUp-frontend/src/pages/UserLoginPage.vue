<template>
  <!-- 居中 -->
  <van-row justify="center">
    <van-form @submit="onSubmit">
      <!-- 居中 -->
      <van-row justify="center">
        <van-image
            :src="team"
            height="5rem"
            position="center"
            round
            width="5rem"/>
      </van-row>
      <van-row justify="center">
        <h3>"寻找志同道合的朋友"</h3>
      </van-row>
      <van-divider/>
      <van-cell-group inset>
        <van-field
            v-model="userAccount"
            name="userAccount"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请填写用户名' }]"
        />
        <van-field
            v-model="userPassword"
            type="password"
            name="userPassword"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          登录
        </van-button>
      </div>
      <van-divider/>
      <van-cell title="" to="/user/register" value="还没有账号？注册"></van-cell>


    </van-form>
  </van-row>
</template>

<script setup>

import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast, Toast} from "vant";
import {ref} from "vue";
import {useRouter} from "vue-router";
import route from "../config/route";
import team from "../assets/team.png";

const router = useRouter();

const userAccount = ref();
const userPassword = ref();

const onSubmit = async () => {
  // console.log("用户登录");
  const res = await myAxios.post("/user/login", {
    userAccount: userAccount.value,
    userPassword: userPassword.value
  });
  if (res.code === 0 && res.data != null) {
    const redirectUrl = route.query?.redirect ?? '/';
    window.location.href = redirectUrl;
  } else {
    showFailToast("用户名或密码错误")
  }
};

const toRegister = () => {
  router.push({
    path: '/user/register',
  })
}

</script>

<style scoped>

</style>
