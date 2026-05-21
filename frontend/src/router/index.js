import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/product/ProductList.vue')
      },
      {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('../views/product/ProductAdd.vue')
      },
      {
        path: 'product/:id/edit',
        name: 'ProductEdit',
        component: () => import('../views/product/ProductEdit.vue')
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/product/ProductDetail.vue')
      },
      {
        path: 'products/manage',
        name: 'ProductManage',
        component: () => import('../views/product/ProductManage.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/cart/Cart.vue')
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: () => import('../views/order/OrderCreate.vue')
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../views/order/OrderList.vue')
      },
      {
        path: 'orders/manage',
        name: 'OrderManage',
        component: () => import('../views/order/OrderManage.vue')
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('../views/order/OrderDetail.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router