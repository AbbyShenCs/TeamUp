/**
 * 用户类别
 */
export type UserType = {
    id: number;
    username: string;
    profile?: string;
    userAccount: string;
    avatarUrl?: string;
    gender: number;
    phone: string;
    email: string;
    userStatus: number;
    userRole: number;
    planetCode: string;
    tags: string;
    createTime: Date;
};
