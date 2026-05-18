import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/dashboard',
    component: () => import('@/views/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/index.vue'),
    children: [
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/admin/building/index.vue')
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/admin/room/index.vue')
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/admin/student/index.vue')
      },
      {
        path: 'manager',
        name: 'Manager',
        component: () => import('@/views/admin/manager/index.vue')
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('@/views/admin/maintenance/index.vue')
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/admin/repair/index.vue')
      },
      {
        path: 'visitor',
        name: 'Visitor',
        component: () => import('@/views/admin/visitor/index.vue')
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('@/views/admin/checkin/index.vue')
      },
      {
        path: 'utility',
        name: 'Utility',
        component: () => import('@/views/admin/utility/index.vue')
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('@/views/admin/health/index.vue')
      },
      {
        path: 'civilized',
        name: 'Civilized',
        component: () => import('@/views/admin/civilized/index.vue')
      },
      {
        path: 'emergency',
        name: 'AdminEmergency',
        component: () => import('@/views/admin/emergency/index.vue')
      },
      {
        path: 'lost-found',
        name: 'AdminLostFound',
        component: () => import('@/views/admin/lost-found/index.vue')
      },
      {
        path: 'repair-type',
        name: 'AdminRepairType',
        component: () => import('@/views/admin/repair-type/index.vue')
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/logs/index.vue')
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/system/index.vue')
      },
      {
        path: 'chat',
        name: 'AdminChat',
        component: () => import('@/views/admin/chat/index.vue')
      },
      {
        path: 'room-change',
        name: 'AdminRoomChange',
        component: () => import('@/views/admin/room-change/index.vue')
      },
      {
        path: 'assignment',
        name: 'Assignment',
        component: () => import('@/views/admin/assignment/index.vue')
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/admin/notice/index.vue')
      },
      {
        path: 'rules',
        name: 'Rules',
        component: () => import('@/views/admin/rules/index.vue')
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/admin/student/index.vue')
      },
      {
        path: 'manager',
        name: 'Manager',
        component: () => import('@/views/admin/manager/index.vue')
      },
      {
        path: 'bed',
        name: 'Bed',
        component: () => import('@/views/admin/bed/index.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/admin/profile/index.vue')
      }
    ]
  },
  {
    path: '/student',
    component: () => import('@/views/student/layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/dashboard/index.vue')
      },
      {
        path: 'repair',
        name: 'StudentRepair',
        component: () => import('@/views/student/repair/index.vue')
      },
      {
        path: 'checkin',
        name: 'StudentCheckIn',
        component: () => import('@/views/student/checkin/index.vue')
      },
      {
        path: 'utility',
        name: 'StudentUtility',
        component: () => import('@/views/student/utility/index.vue')
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/profile/index.vue')
      },
      {
        path: 'message',
        name: 'StudentMessage',
        component: () => import('@/views/student/message/index.vue')
      },
      {
        path: 'visitor',
        name: 'StudentVisitor',
        component: () => import('@/views/student/visitor/index.vue')
      },
      {
        path: 'room-change',
        name: 'StudentRoomChange',
        component: () => import('@/views/student/room-change/index.vue')
      },
      {
        path: 'announcement',
        name: 'StudentAnnouncement',
        component: () => import('@/views/student/announcement/index.vue')
      },
      {
        path: 'rules',
        name: 'StudentRules',
        component: () => import('@/views/student/rules/index.vue')
      },
      {
        path: 'civilized',
        name: 'StudentCivilized',
        component: () => import('@/views/student/civilized/index.vue')
      },
      {
        path: 'health',
        name: 'StudentHealth',
        component: () => import('@/views/student/health/index.vue')
      },
      {
        path: 'emergency',
        name: 'StudentEmergency',
        component: () => import('@/views/student/emergency/index.vue')
      },
      {
        path: 'lost-found',
        name: 'StudentLostFound',
        component: () => import('@/views/student/lost-found/index.vue')
      },
      {
        path: 'chat',
        name: 'StudentChat',
        component: () => import('@/views/student/chat/index.vue')
      }
    ]
  },
  {
    path: '/manager',
    component: () => import('@/views/manager/layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'ManagerDashboard',
        component: () => import('@/views/manager/dashboard/index.vue')
      },
      {
        path: 'student',
        name: 'ManagerStudent',
        component: () => import('@/views/manager/student/index.vue')
      },
      {
        path: 'repair',
        name: 'ManagerRepair',
        component: () => import('@/views/manager/repair/index.vue')
      },
      {
        path: 'checkin',
        name: 'ManagerCheckIn',
        component: () => import('@/views/manager/checkin/index.vue')
      },
      {
        path: 'utility',
        name: 'ManagerUtility',
        component: () => import('@/views/manager/utility/index.vue')
      },
      {
        path: 'visitor',
        name: 'ManagerVisitor',
        component: () => import('@/views/manager/visitor/index.vue')
      },
      {
        path: 'health',
        name: 'ManagerHealth',
        component: () => import('@/views/manager/health/index.vue')
      },
      {
        path: 'room-change',
        name: 'ManagerRoomChange',
        component: () => import('@/views/manager/room-change/index.vue')
      },
      {
        path: 'notice',
        name: 'ManagerNotice',
        component: () => import('@/views/student/announcement/index.vue')
      },
      {
        path: 'profile',
        name: 'ManagerProfile',
        component: () => import('@/views/manager/profile/index.vue')
      },
      {
        path: 'civilized',
        name: 'ManagerCivilized',
        component: () => import('@/views/manager/civilized/index.vue')
      },
      {
        path: 'emergency',
        name: 'ManagerEmergency',
        component: () => import('@/views/manager/emergency/index.vue')
      }
    ]
  },
  {
    path: '/maintenance',
    component: () => import('@/views/maintenance/layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'MaintenanceDashboard',
        component: () => import('@/views/maintenance/dashboard/index.vue')
      },
      {
        path: 'repair',
        name: 'MaintenanceRepair',
        component: () => import('@/views/maintenance/repair/index.vue')
      },
      {
        path: 'profile',
        name: 'MaintenanceProfile',
        component: () => import('@/views/maintenance/profile/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
