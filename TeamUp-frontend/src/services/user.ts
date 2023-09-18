import myAxios from "../plugins/myAxios";
import {getCurrentUserState, setCurrentUserState} from "../states/user";

/**
 * 获取用户信息
 * @returns {Promise<null|any>}
 */
export const getCurrentUser = async () => {
    const user = getCurrentUserState();
    if (user) {
        return user;
    }
    //从远程处获取用户信息
    const res = await myAxios.get("/user/current");
    if (res.code == 0 ) {
        // setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}
