package com.dormitory;

import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DataInitializer - 系统数据初始化类
 * 
 * 在应用启动时自动初始化系统基础数据，包括管理员、宿管、学生、
 * 楼栋、宿舍、床位、报修类型、通知公告、宿舍规则、水电费阈值等
 * 使用CommandLineRunner在Spring Boot启动后执行
 * 
 * @author 王和友
 * @since 2026
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final DormitoryManagerMapper managerMapper;
    private final StudentMapper studentMapper;
    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final RepairTypeMapper repairTypeMapper;
    private final NoticeMapper noticeMapper;
    private final DormitoryRuleMapper dormitoryRuleMapper;
    private final UtilityThresholdMapper utilityThresholdMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminMapper adminMapper,
                          DormitoryManagerMapper managerMapper,
                          StudentMapper studentMapper,
                          BuildingMapper buildingMapper,
                          RoomMapper roomMapper,
                          BedMapper bedMapper,
                          RepairTypeMapper repairTypeMapper,
                          NoticeMapper noticeMapper,
                          DormitoryRuleMapper dormitoryRuleMapper,
                          UtilityThresholdMapper utilityThresholdMapper,
                          PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.managerMapper = managerMapper;
        this.studentMapper = studentMapper;
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.repairTypeMapper = repairTypeMapper;
        this.noticeMapper = noticeMapper;
        this.dormitoryRuleMapper = dormitoryRuleMapper;
        this.utilityThresholdMapper = utilityThresholdMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 初始化系统数据
     * 检查数据库是否已有数据，如有则跳过初始化，否则执行初始化
     * 
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        if (adminMapper.selectCount(null) > 0) {
            return;
        }
        
        initAdmin();
        initManager();
        initStudents();
        initBuildings();
        initRooms();
        initBeds();
        initRepairTypes();
        initNotices();
        initRules();
        initUtilityThresholds();
    }

    private void initAdmin() {
        if (adminMapper.selectCount(null) > 0) return;
        
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("系统管理员");
        admin.setPhone("13800138000");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminMapper.insert(admin);
    }

    private void initManager() {
        if (managerMapper.selectCount(null) > 0) return;
        
        DormitoryManager manager = new DormitoryManager();
        manager.setUsername("manager1");
        manager.setPassword(passwordEncoder.encode("123456"));
        manager.setName("张宿管");
        manager.setGender(1);
        manager.setPhone("13900139000");
        manager.setBuildingId(1L);
        manager.setStatus(1);
        manager.setCreateTime(LocalDateTime.now());
        manager.setUpdateTime(LocalDateTime.now());
        managerMapper.insert(manager);
    }

    private void initStudents() {
        if (studentMapper.selectCount(null) > 0) return;
        
        String[][] students = {
            {"2024001", "张三", "男", "计算机学院", "软件工程", "软件2401班", "13700137001"},
            {"2024002", "李四", "男", "计算机学院", "软件工程", "软件2401班", "13700137002"},
            {"2024003", "王五", "女", "计算机学院", "计算机科学", "计科2401班", "13700137003"},
            {"2024004", "赵六", "女", "信息学院", "电子信息", "电信2401班", "13700137004"}
        };
        
        for (String[] s : students) {
            Student student = new Student();
            student.setStudentNumber(s[0]);
            student.setPassword(passwordEncoder.encode("123456"));
            student.setName(s[1]);
            student.setGender("男".equals(s[2]) ? 1 : 2);
            student.setCollege(s[3]);
            student.setMajor(s[4]);
            student.setClassName(s[5]);
            student.setPhone(s[6]);
            student.setStatus(1);
            student.setCreateTime(LocalDateTime.now());
            student.setUpdateTime(LocalDateTime.now());
            studentMapper.insert(student);
        }
    }

    private void initBuildings() {
        if (buildingMapper.selectCount(null) > 0) return;
        
        Building b1 = new Building();
        b1.setBuildingName("1号楼");
        b1.setBuildingNumber("A1");
        b1.setFloorCount(6);
        b1.setRoomCount(60);
        b1.setManagerId(1L);
        b1.setCreateTime(LocalDateTime.now());
        b1.setUpdateTime(LocalDateTime.now());
        buildingMapper.insert(b1);

        Building b2 = new Building();
        b2.setBuildingName("2号楼");
        b2.setBuildingNumber("A2");
        b2.setFloorCount(6);
        b2.setRoomCount(60);
        b2.setManagerId(1L);
        b2.setCreateTime(LocalDateTime.now());
        b2.setUpdateTime(LocalDateTime.now());
        buildingMapper.insert(b2);

        Building b3 = new Building();
        b3.setBuildingName("3号楼");
        b3.setBuildingNumber("B1");
        b3.setFloorCount(5);
        b3.setRoomCount(50);
        b3.setManagerId(null);
        b3.setCreateTime(LocalDateTime.now());
        b3.setUpdateTime(LocalDateTime.now());
        buildingMapper.insert(b3);
    }

    private void initRooms() {
        if (roomMapper.selectCount(null) > 0) return;
        
        for (int i = 1; i <= 2; i++) {
            for (int j = 1; j <= 2; j++) {
                Room room = new Room();
                room.setBuildingId(1L);
                room.setRoomNumber(i + "0" + j);
                room.setFloor(i);
                room.setRoomType(4);
                room.setBedCount(4);
                room.setCurrentCount(0);
                room.setStatus(1);
                room.setCreateTime(LocalDateTime.now());
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.insert(room);
            }
        }
        
        for (int i = 1; i <= 2; i++) {
            for (int j = 1; j <= 2; j++) {
                Room room = new Room();
                room.setBuildingId(2L);
                room.setRoomNumber(i + "0" + j);
                room.setFloor(i);
                room.setRoomType(6);
                room.setBedCount(6);
                room.setCurrentCount(0);
                room.setStatus(1);
                room.setCreateTime(LocalDateTime.now());
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.insert(room);
            }
        }
    }

    private void initBeds() {
        if (bedMapper.selectCount(null) > 0) return;
        
        for (long roomId = 1; roomId <= 4; roomId++) {
            for (int bedNum = 1; bedNum <= 4; bedNum++) {
                Bed bed = new Bed();
                bed.setRoomId(roomId);
                bed.setBedNumber(bedNum);
                bed.setStudentId(null);
                bed.setStatus(0);
                bed.setCreateTime(LocalDateTime.now());
                bed.setUpdateTime(LocalDateTime.now());
                bedMapper.insert(bed);
            }
        }
        
        for (long roomId = 5; roomId <= 6; roomId++) {
            for (int bedNum = 1; bedNum <= 6; bedNum++) {
                Bed bed = new Bed();
                bed.setRoomId(roomId);
                bed.setBedNumber(bedNum);
                bed.setStudentId(null);
                bed.setStatus(0);
                bed.setCreateTime(LocalDateTime.now());
                bed.setUpdateTime(LocalDateTime.now());
                bedMapper.insert(bed);
            }
        }
    }

    private void initRepairTypes() {
        if (repairTypeMapper.selectCount(null) > 0) return;
        
        String[] types = {"水电维修", "门锁维修", "家具维修", "网络故障", "空调维修", "其他"};
        String[] icons = {"water", "lock", "chair", "wifi", "snowflake", "ellipsis"};
        
        for (int i = 0; i < types.length; i++) {
            RepairType type = new RepairType();
            type.setTypeName(types[i]);
            type.setTypeIcon(icons[i]);
            type.setSortOrder(i + 1);
            type.setStatus(1);
            type.setCreateTime(LocalDateTime.now());
            type.setUpdateTime(LocalDateTime.now());
            repairTypeMapper.insert(type);
        }
    }

    private void initNotices() {
        if (noticeMapper.selectCount(null) > 0) return;
        
        Notice n1 = new Notice();
        n1.setTitle("欢迎使用智能宿舍管理系统");
        n1.setContent("本系统为学生提供宿舍管理服务，包括报修、打卡、换寝等功能。");
        n1.setNoticeType("system");
        n1.setIsTop(1);
        n1.setPublisherId(1L);
        n1.setCreateTime(LocalDateTime.now());
        n1.setUpdateTime(LocalDateTime.now());
        noticeMapper.insert(n1);

        Notice n2 = new Notice();
        n2.setTitle("宿舍安全提示");
        n2.setContent("请勿使用大功率电器，离开宿舍请锁好门窗，注意防火防盗。");
        n2.setNoticeType("notice");
        n2.setIsTop(0);
        n2.setPublisherId(1L);
        n2.setCreateTime(LocalDateTime.now());
        n2.setUpdateTime(LocalDateTime.now());
        noticeMapper.insert(n2);
    }

    private void initRules() {
        if (dormitoryRuleMapper.selectCount(null) > 0) return;
        
        DormitoryRule r1 = new DormitoryRule();
        r1.setBuildingId(null);
        r1.setTitle("作息规定");
        r1.setContent("晚上11点前必须回宿舍，周末可延长至12点。");
        r1.setRuleType(1);
        r1.setCreateTime(LocalDateTime.now());
        r1.setUpdateTime(LocalDateTime.now());
        dormitoryRuleMapper.insert(r1);

        DormitoryRule r2 = new DormitoryRule();
        r2.setBuildingId(null);
        r2.setTitle("卫生规定");
        r2.setContent("每周进行一次大扫除，保持宿舍整洁卫生。");
        r2.setRuleType(2);
        r2.setCreateTime(LocalDateTime.now());
        r2.setUpdateTime(LocalDateTime.now());
        dormitoryRuleMapper.insert(r2);

        DormitoryRule r3 = new DormitoryRule();
        r3.setBuildingId(null);
        r3.setTitle("安全规定");
        r3.setContent("严禁使用大功率电器，严禁私拉乱接电线。");
        r3.setRuleType(3);
        r3.setCreateTime(LocalDateTime.now());
        r3.setUpdateTime(LocalDateTime.now());
        dormitoryRuleMapper.insert(r3);
    }

    private void initUtilityThresholds() {
        if (utilityThresholdMapper.selectCount(null) > 0) return;
        
        UtilityThreshold t1 = new UtilityThreshold();
        t1.setRoomType(4);
        t1.setElectricLimit(new java.math.BigDecimal("50.00"));
        t1.setWaterLimit(new java.math.BigDecimal("10.00"));
        t1.setElectricPrice(new java.math.BigDecimal("0.50"));
        t1.setWaterPrice(new java.math.BigDecimal("2.00"));
        t1.setCreateTime(LocalDateTime.now());
        t1.setUpdateTime(LocalDateTime.now());
        utilityThresholdMapper.insert(t1);

        UtilityThreshold t2 = new UtilityThreshold();
        t2.setRoomType(6);
        t2.setElectricLimit(new java.math.BigDecimal("80.00"));
        t2.setWaterLimit(new java.math.BigDecimal("15.00"));
        t2.setElectricPrice(new java.math.BigDecimal("0.50"));
        t2.setWaterPrice(new java.math.BigDecimal("2.00"));
        t2.setCreateTime(LocalDateTime.now());
        t2.setUpdateTime(LocalDateTime.now());
        utilityThresholdMapper.insert(t2);
    }
}
