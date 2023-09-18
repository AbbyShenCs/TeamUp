<template>
  <form action="/">
  </form>
  <van-divider content-position="left">已选标签</van-divider>
  <van-divider v-if="activeIds.length===0" content-position="center">请选择标签</van-divider>
  <van-row gutter="15" style="padding: 0 15px">
    <van-col v-for="tag in activeIds">
      <van-tag closeable size="small" type="primary" @close="doClose(tag)">
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>

  <van-divider content-position="left">可选择标签</van-divider>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagsList"
  />
  <div style="padding: 15px">
    <van-button block type="primary" @click="updateTags">确定</van-button>
  </div>

</template>

<script setup>
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";
import UserTagsList from "../constants/UserTagsList";
import myAxios from "../plugins/myAxios";
import qs from "qs";
import {showFailToast} from "vant";
import {getCurrentUser} from "../services/user";

const router = useRouter();
const activeIds = ref([]);
const activeIndex = ref(0);
//原数据
const originTagsList = UserTagsList
const tagsList = ref(originTagsList);
const currentUser = ref();

onMounted(async () => {     //异步调用
  currentUser.value = await getCurrentUser();
  originTagsList.value = JSON.parse(currentUser.value.tags)
  activeIds.value = originTagsList.value
  console.log('originTagsList.value' + originTagsList.value )
})

//移除标签
const doClose = (tag) => {
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag;
  })
}

const updateTags = async () => {
  currentUser.value = await getCurrentUser();
  const res = await myAxios.get('/user/updateTags', {
    params: {
      id: currentUser.value.id,
      tagNameList: activeIds.value
    },
    paramsSerializer: {
      serialize: function (params) {
        // return qs.stringify(params,{arrayFormat:'repeat'})
        return qs.stringify(params, {indices: false})
      }
    }
  })
  console.log("修改用户信息", res);
  if (res.code === 0 && res.data > 0) {
    await router.replace("/user");
  } else {
    showFailToast("更新失败")
  }
};
</script>

<style scoped>

</style>
