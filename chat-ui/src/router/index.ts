import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import NameView from "../views/NameView.vue";
import ChatView from "../views/ChatView.vue";
import LogIn from "@/views/LogIn.vue";
import Register from "@/views/Register.vue";
import ReserveMeeting from "@/views/ReserveMeeting.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "name",
    component: LogIn,
    meta: {
      keepAlive: true,
    },
  },
  {
    path: "/chat",
    name: "chat",
    component: ChatView,
    meta: {
      keepAlive: true,
    },
  },
  {
    path: "/login",
    name: "LogIn",
    component: LogIn,
    meta: {
      keepAlive: true,
    },
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/reserve",
    name: "Reserve",
    component: ReserveMeeting,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
