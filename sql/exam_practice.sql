/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : exam_practice

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 27/12/2025 15:30:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for practice_record
-- ----------------------------
DROP TABLE IF EXISTS `practice_record`;
CREATE TABLE `practice_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否正确',
  `cost_time` int NULL DEFAULT NULL COMMENT '答题耗时（秒）',
  `practice_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_correct`(`is_correct` ASC) USING BTREE,
  INDEX `idx_time`(`practice_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '练习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of practice_record
-- ----------------------------

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `owner_id` bigint NULL DEFAULT NULL COMMENT '题目所属用户ID(NULL表示公共题库)',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题型：choice(选择题)/judge(判断题)',
  `display_order` int NULL DEFAULT 0 COMMENT '显示顺序（按类型分组，从1开始）',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '未分类' COMMENT '科目',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目内容',
  `options` json NULL COMMENT '选项（选择题用，格式：[\"A:选项1\",\"B:选项2\",\"C:选项3\",\"D:选项4\"]）',
  `answer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析',
  `difficulty` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '难度：easy/medium/hard',
  `is_marked` tinyint(1) NULL DEFAULT 0 COMMENT '是否标记为重点',
  `wrong_count` int NULL DEFAULT 0 COMMENT '做错次数',
  `practice_count` int NULL DEFAULT 0 COMMENT '练习次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_subject`(`subject` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE,
  INDEX `idx_type_display_order`(`type` ASC, `display_order` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1766 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (1665, NULL, 'single-choice', 1, '1', '马克思是关于自然界，人类社会，人类思维发展的（）的理论体系。', '[\"A:一般规律\", \"B:特殊规律\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1666, NULL, 'multiple-choice', 1, '1', '（多选）中国革命建设和改革的实践证明，要运用马克思主义指导实践，必须实现马克思主义中国化，马克思之所以能够中国化的原因在于（）', '[\"A:马克思主义理论的内在要求\", \"B:马克思主义与中华民族优秀文化具有相融性\", \"C:中国革命建设和改革的实践需要马克思主义指导\", \"D:马克思主义为中国革命建设和改革提供了现实发展模式\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1667, NULL, 'multiple-choice', 2, '1', '（多选）马克思主义中国化就是把马克思主义基本原理同中国具体实际和时代特征结合起来（）', '[\"A:运用马克思主义的立场、观点、方法研究和解决中国革命、建设、改革中的实际问题\", \"B:就是总结和提炼中国革命、建设、改革的实践经验，从而认识和掌握客观规律，为马克思主义理论宝库增添新的内容\", \"C:是运用中国人民喜闻乐见的民族语言来阐述马克思主义理论，使之成为具有中国特色、中国风格、中国气派的马克思主义\", \"D:就是严格按照马克思、恩格斯经典著作中的重要论断建设中国的社会主义事业\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1668, NULL, 'single-choice', 2, '1', '马克思主义中国化第一次历史性飞跃的理论成果是（）', '[\"A:毛泽东思想\", \"B:新三民主义\", \"C:邓小平理论\", \"D:习近平新时代中国特色社会主义思想\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1669, NULL, 'single-choice', 3, '1', '在中国共产党的历史上，()第一个明确提出了“马克思主义中国化”的科学命题和重大任务，深刻论证了马克思主义中国化的必要性和极端重要性。', '[\"A:陈独秀\", \"B:李大钊\", \"C:毛泽东\", \"D:刘少奇\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1670, NULL, 'multiple-choice', 3, '1', '（多选）党的十九大概括了习近平新时代中国特色社会主义思想的丰富内涵，包括（）', '[\"A:“一个中心”\", \"B:“两个基本点”\", \"C:“十个明确”\", \"D:“十四个坚持”\"]', 'CD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1671, NULL, 'multiple-choice', 4, '1', '（多选）在中国共产党第二十次全国代表大会上的报告中，习近平指出：“中国共产党人深刻认识到，只有把马克思主义基本原理同()相结合、同()相结合，坚持运用辩证唯物主义和历史唯物主义，才能正确回答时代和实践提出的重大问题，才能始终保持马克思主义的蓬勃生机和旺盛活力。', '[\"A:中国具体实际\", \"B:中华优秀传统文化\", \"C:时代特征\", \"D:中国精神\"]', 'AB', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1672, NULL, 'single-choice', 4, '1', '党的十九大报告指出，经过长期努力，中国特色社会主义进入了（），这是我国发展新的历史方位。', '[\"A:新时代\", \"B:新纪元\", \"C:新阶段\", \"D:新时期\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1673, NULL, 'single-choice', 5, '1', '中国共产党团结带领中国人民，浴血奋战、百折不挠，创造了新民主主义革命的伟大成就，为实现中华民族伟大复兴（）', '[\"A:创造了根本社会条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1674, NULL, 'single-choice', 6, '1', '中国共产党团结带领中国人民，自力更生、发愤图强，创造了社会主义革命和建设的伟大成就，为实现中华民族伟大复兴（）', '[\"A:创造了根本社会条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1675, NULL, 'single-choice', 7, '1', '中国共产党团结带领中国人民，解放思想、锐意进取，创造了改革开放和社会主义现代化建设的伟大成就，为实现中华民族伟大复兴()。', '[\"A:提供了充满新的活力的体制保证和快速发展的物质条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\", \"D:创造了根本社会条件\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1676, NULL, 'single-choice', 8, '1', '中国共产党团结带领中国人民，自信自强、守正创新，统揽伟大斗争、伟大工程、伟大事业、伟大梦想，创造了新时代中国特色社会主义的伟大成就，为实现中华民族伟大复兴()。', '[\"A:奠定了根本政治前提和制度基础\", \"B:创造了根本社会条件\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1677, NULL, 'single-choice', 9, '1', '为了实现中华民族伟大复兴，中国共产党团结带领中国人民，浴血奋战、百折不挠，创造了新民主主义革命的伟大成就。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告：', '[\"A:改革开放是决定当代中国前途命运的关键一招，中国大踏步赶上了时代！\", \"B:中国人民站起来了，中华民族任人宰割、饱受欺凌的时代一去不复返了！\", \"C:中国人民不但善于破坏一个旧世界、也善于建设一个新世界，只有社会主义才能救中国，只有中国特色社会主义才能发展中国！\", \"D:中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复兴进入了不可逆转的历史进程！\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1678, NULL, 'single-choice', 10, '1', '为了实现中华民族伟大复兴，中国共产党团结带领中国人民，自力更生、发愤图强，创造了社会主义革命和建设的伟大成就。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告：', '[\"A:中国人民站起来了，中华民族任人宰割、饱受欺凌的时代一去不复返了！\", \"B:改革开放是决定当代中国前途命运的关键一招，中国大踏步赶上了时代！\", \"C:中国人民不但善于破坏一个旧世界、也善于建设一个新世界，只有社会主义才能救中国，只有中国特色社会主义才能发展中国！\", \"D:中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复业进入了不可逆转的历史进程\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1679, NULL, 'single-choice', 11, '1', '为了实现中华民族伟大复兴，中国共产党团结带领中国人民，解放思想、锐意进取，创造了改革开放和社会主义现代化建设的伟大成就。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告', '[\"A:中国人民站起来了，中华民族任人宰割、饱受欺凌的时代一去不复返了！\", \"B:中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复兴进入了不可逆转的历史进程！\", \"C:改革开放是决定当代中国前途命运的关键一招，中国大踏步赶上了时代！\", \"D:中国人民不但善于破坏一个旧世界、也善于建设一个新世界，只有社会主义才能救中国，只有中国特色社会主义才能发展中国！\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1680, NULL, 'single-choice', 12, '1', '为了实现中华民族伟大复兴，中国共产党团结带领中国人民，自信自强、守正创新，统揽伟大斗争、伟大工程、伟大事业、伟大梦想，创造了新时代中国特色社会主义的伟大成就。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告：', '[\"A:中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复兴进入了不可逆转的历史进程！\", \"B:改革开放是决定当代中国前途命运的关键一招，中国大踏步赶上了时代！\", \"C:中国人民不但善于破坏一个旧世界、也善于建设一个新世界，只有社会主义才能救中国，只有中国特色社会主义才能发展中国！\", \"D:中国人民站起来了，中华民族任人宰割、饱受欺凌的时代一去不复返了！\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1681, NULL, 'single-choice', 13, '1', '2012年11月29日，习近平率中央政治局常委和中央书记处的同志来到国家博物馆，参观()展览，明确提出实现中华民族伟大复兴，就是中华民族近代以来最伟大的梦想', '[\"A:《鸦片战争》\", \"B:《五四运动》\", \"C:《复兴之路》\", \"D:《开天辟地》\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1682, NULL, 'single-choice', 14, '1', '近代历来中华民族最伟大的梦想是（）', '[\"A:实现中华民族伟大复兴\", \"B:实现国家富强\", \"C:实现中华民族伟大振兴\", \"D:全面建成小康社会\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1683, NULL, 'multiple-choice', 5, '1', '(多选）坚持和发展中国特色社会主义总任务，是（）', '[\"A:实现社会主义现代化\", \"B:实现中华民族伟大复兴\", \"C:全面建成小康社会\", \"D:建设社会主义法制国家\"]', 'AB', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1684, NULL, 'multiple-choice', 6, '1', '中国梦的本质是（）', '[\"A:国家富强\", \"B:民族振兴\", \"C:人民幸福\", \"D:经济发展\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1685, NULL, 'multiple-choice', 7, '1', '(多选）科学社会主义基本原则不能丢，丢了就不是社会主义。下列属于科学社会主义基本原则的是：', '[\"A:无产阶级政党是无产阶级的先锋队，社会主义事业必须坚持无产阶级政党的领导\", \"B:无产阶级革命是无产阶级进行斗争的最高形式，必须以建立无产阶级专政的国家为目的\", \"C:在生产资料公有制基础上组织生产，满足全体社会成员的需要是社会主义生产的根本目的\", \"D:对社会生产进行有计划的指导和调节，实行等量劳动领取等量产品的按劳分配原则\", \"E:通过无产阶级专政和社会主义高度发展最终实现向消灭阶级、消灭剥削、实现人的全面而自由发展的共产主义社会的过渡\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1686, NULL, 'multiple-choice', 8, '1', '（多选）我们党领导人民进行社会主义建设，有改革开放前和改革开放后两个历史时期，这两个历史时期的关系', '[\"A:如果没有1949年建立新中国并进行社会主义革命和建设，积累了重要的思想、物质、制度条件，积累了正反两方面经验，改革开放也很难顺利推进\", \"B:虽然这两个历史时期在进行社会主义建设的思想指导、方针政策、实际工作上有很大差别，但两者决不是彼此割裂的，更不是根本对立的\", \"C:不能用改革开放后的历史时期否定改革开放前的历史时期\", \"D:不能用改革开放前的历史时期否定改革开放后的历史时期\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1687, NULL, 'multiple-choice', 9, '1', '中国共产党第二十次全国代表大会报告指出，中国式现代化，是中国共产党领导的社会主义现代化，既有各国现代化的共同特征，更有基于自己国情的中国特色。中国式现代化的特色是：', '[\"A:中国式现代化是人口规模巨大的现代化\", \"B:中国式现代化是全体人民共同富裕的现代\", \"C:中国式现代化是物质文明和精神文明相协调的现代化\", \"D:中国式现代化是人与自然和谐共生的现代\", \"E:中国式现代化是走和平发展道路的现代化\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1688, NULL, 'single-choice', 15, '1', '在现阶段，我国社会主要矛盾已经转化为人民日益增长的()需要和（）的发展之间的矛盾', '[\"A:美好生活；不充分不平衡\", \"B:幸福生活；不平衡不充分\", \"C:幸福生活；不充分不平衡\", \"D:美好生活；不平衡不充分\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1689, NULL, 'single-choice', 16, '1', '()是中国特色社会主义最本质的特征，是中国特色社会主义制度的最大优势。', '[\"A:中国共产党的领导\", \"B:以经济建设为中心\", \"C:人民利益为根本出发点\", \"D:“五位一体”总体布局\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1690, NULL, 'single-choice', 17, '1', '习近平新时代中国特色社会主义思想，明确中国特色社会主义最本质的特征是中国共产党领导，中国特色社会主义制度的最大优势是中国共产党领导，______是最高政治领导力量，全党必须增强“四个意识”、坚定“四个自信”、做到“两个维护”。', '[\"A:中国共产党\", \"B:工人阶级\", \"C:人民群众\", \"D:全国人民代表大会\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1691, NULL, 'single-choice', 18, '1', '中国特色社会主义新时代是我国发展（）', '[\"A:新的历史方位\", \"B:新的历史时期\", \"C:新的历史阶段\", \"D:新的时空定位\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1692, NULL, 'multiple-choice', 10, '1', '(多选）习近平新时代中国特色社会主义思想回答的重大时代课题有', '[\"A:新时代坚持和发展什么样的中国特色社会主义、怎样坚持和发展中国特色社会主义\", \"B:建设什么样的社会主义现代化强国、怎样建设社会主义现代化强国\", \"C:建设什么样的长期执政的马克思主义政党、怎样建设长期执政的马克思主义政党\", \"D:什么是社会主义，怎样建设社会主义\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1693, NULL, 'single-choice', 19, '1', '2021年7月1日，习近平在庆祝中国共产党成立100周年大会上的讲话中强调，中国共产党一经诞生，就把为中国人民谋幸福、为中华民族谋复兴确立为自己的初心使命。一百年来，中国共产党团结带领中国人民进行的一切奋斗、一切牺牲、一切创造，归结起来就是一个主题：()。', '[\"A:坚持和发展中国特色社会主义\", \"B:实现中华民族伟大复兴\", \"C:解放和发展生产力\", \"D:共同富裕\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1694, NULL, 'single-choice', 20, '1', '近些年来，国内外有些舆论提出中国现在搞的究竟还是不是社会主义的疑问，有人说是“资本社会主义”，还有人干脆说是“国家资本主义”、“新官僚资本主义”。你认为这些观点()', '[\"A:都是完全错误的\", \"B:都是完全正确的\", \"C:都是有一定道理的\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1695, NULL, 'multiple-choice', 11, '1', '（多选）党的领导是全面的。关于这一句的理解正确的是', '[\"A:领导对象要全面覆盖\", \"B:领导内容要全面\", \"C:领导过程要全面\", \"D:领导方法要全面\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1696, NULL, 'single-choice', 21, '1', '必须认识到，我国社会主要矛盾的变化，没有改变我们对我国社会主义所处历史阶段的判断，我国仍处于并将长期处于___的基本国情没有变，我国是世界最大发展中国家的国际地位没有变。', '[\"A:社会主义阶段\", \"B:社会主义初级阶段\", \"C:社会主义中级阶段\", \"D:社会主义高级阶段\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1697, NULL, 'single-choice', 22, '1', '新时代中国特色社会主义思想，明确中国特色社会主义最本质的特征是___。', '[\"A:解放生产力，发展生产力\", \"B:共同富裕\", \"C:马克思主义为指导\", \"D:中国共产党领导\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1698, NULL, 'single-choice', 23, '1', '习近平新时代中国特色社会主义思想，明确坚持和发展中国特色社会主义，总任务是实现社会主义现代化和中华民族伟大复兴，在全面建成小康社会的基础上，分两步走在本世纪中叶建成富强民主文明和谐美丽的社会主义现代化强国，以_ 推进中华民族伟大复兴。()', '[\"A:西方式现代化\", \"B:中国式现代化\", \"C:苏联式现代化\", \"D:东方式现代化\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1699, NULL, 'single-choice', 24, '1', '习近平新时代中国特色社会主义思想，明确新时代我国社会主要矛盾是人民日益增长的美好生活需要和不平衡不充分的发展之间的矛盾，必须坚持以人民为中心的发展思想，发展_____民主，推动人的全面发展、全体人民共同富裕取得更为明显的实质性进展。()', '[\"A:全员人民\", \"B:全链条人民\", \"C:全过程人民\", \"D:全方位人民\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1700, NULL, 'single-choice', 25, '1', '习近平新时代中国特色社会主义思想，明确中国特色社会主义事业总体布局是经济建设、政治建设、文化建设、社会建设、生态文明建设五位一体，战略布局是_____、全面深化改革、全面依法治国、全面从严治党四个全面。()', '[\"A:全面建成社会主义现代化强国\", \"B:全面建设社会主义现代化国家\", \"C:全面建设小康社会\", \"D:全面建成小康社会\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1701, NULL, 'single-choice', 26, '1', '中国共产党以马克思列宁主义、毛泽东思想、邓小平理论、“三个代表”重要思想、科学发展观、____ 作为自己的行动指南。()', '[\"A:习近平新时期中国特色社会主义思想\", \"B:习近平新时代中国特色社会主义思想\", \"C:新时代中国特色社会主义思想\", \"D:新时期中国特色社会主义思想\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1702, NULL, 'single-choice', 27, '1', '毛泽东思想是马克思列宁主义在中国的创造性运用和发展，是被实践证明了的关于中国革命和建设的正确的理论原则和经验总结，是马克思主义中国化的第____次历史性飞跃。（）', '[\"A:—\", \"B:二\", \"C:四\", \"D:三\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1703, NULL, 'multiple-choice', 12, '1', '(多选）习近平新时代中国特色社会主义思想的历史地位', '[\"A:马克思主义中国化新的飞跃\", \"B:当代中国的马克思主义、二十一世纪马克主义\", \"C:中华文化和中国精神的时代精华\", \"D:人类社会发展的科学指南\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1704, NULL, 'single-choice', 28, '1', '综合分析国际国内形势和我国发展条件，从二○二0年到本世纪中叶可以分两个阶段来安排。第二个阶段，从()，在基本实现现代化的基础上，再奋斗十五年，把我国建成富强民主文明和谐美丽的社会主义现代化强国。', '[\"A:2030年2045年\", \"B:2035年本世纪中叶\", \"C:2020年2035年\", \"D:2035年2050年\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1705, NULL, 'single-choice', 29, '1', '习近平新时代中国特色社会主义思想，是', '[\"A:马列主义中国化\", \"B:科学社会主义中国化\", \"C:社会主义中国化\", \"D:马克思主义中国化\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1706, NULL, 'single-choice', 30, '1', '党的十八届三中全会通过了《中共中央关于全面深化改革若干重大问题的决定》，提出全面深化改革的总目标是()。', '[\"A:完善和发展中国特色社会主义制度，推进科技和教育现代化\", \"B:完善和发展中国特色社会主义制度，推进农业和工业现代化\", \"C:完善和发展中国特色社会主义制度，推进国家治理体系和治理能力现代化\", \"D:完善和发展中国特色社会主义制度，推进国防和军队现代化\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1707, NULL, 'single-choice', 31, '1', '党政军民学，东西南北中，党是领导一切的。必须增强___，自觉维护党中央权威和集中统一领导，自觉在思想上政治上行动上同党中央保持高度一致。()', '[\"A:政治意识、全局意识、权威意识、看齐意\", \"B:政治意识、全局意识、核心意识、看齐意\", \"C:政治意识、大局意识、核心意识、看齐意\", \"D:政治意识、大局意识、权威意识、看齐意识\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1708, NULL, 'single-choice', 32, '1', '综合分析国际国内形势和我国发展条件，从二〇二0年到本世纪中叶可以分两个阶段来安排。第一个阶段，从()，在全面建成小康社会的基础上，再奋斗十五年，基本实现社会主义现代化。', '[\"A:2030年2045年\", \"B:2035年到本世纪中叶\", \"C:2020年2035年\", \"D:2025年2040年\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1709, NULL, 'single-choice', 33, '1', '2022年10月16日，习近平在中国共产党第二十次全国代表大会上的报告中指出，()是实现中华民族伟大复兴的必由之路。', '[\"A:全面从严治党\", \"B:团结奋斗\", \"C:贯彻新发展理念\", \"D:中国特色社会主义\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1710, NULL, 'multiple-choice', 13, '1', '(多选）党是最高的政治领导力量，对这句话理解正确的是()。', '[\"A:这是推进伟大事业的根本保证\", \"B:这是对历史经验的深刻总结\", \"C:归根到底是由近代以来中国发展的历史逻辑、理论逻辑、实践逻辑决定的\", \"D:这是马克思主义政党学说的基本原则\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1711, NULL, 'multiple-choice', 14, '1', '(多选）中国共产党第二十次全国代表大会报告指出，十八大召开至今已经十年了。十年来，我们经历了对党和人民事业具有重大现实意义和深远历史意义的三件大事是：', '[\"A:中国特色社会主义进入新时代\", \"B:完成脱贫攻坚、全面建成小康社会的历史任务实现第一个百年奋斗目标A\", \"C:迎来中国共产党成立一百周年\", \"D:基本实现了社会主义现代化\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1712, NULL, 'single-choice', 34, '1', '党确立习近平同志党中央的核心、全党的核心地位，确立习近平新时代中国特色社会主义思想的指导地位，反映了全党全军全国各族人民共同心愿，对新时代党和国家事业发展、对推进中华民族伟大复兴历史进程具有_____意义。', '[\"A:现实\", \"B:决定性\", \"C:历史\", \"D:重要\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1713, NULL, 'single-choice', 35, '1', '我们党历史这么长、规模这么大、执政这么久，如何跳出治乱兴衰的历史周期率?毛泽东同志在延安的窑洞里给出了第一个答案，这就是只有让人民来监督政府，政府才不敢松懈。经过百年奋斗特别是党的十八大以来新的实践，我们党又给出了第二个答案，这就是()', '[\"A:自我批评\", \"B:社会革命\", \"C:自我革命\", \"D:从严治党\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1714, NULL, 'single-choice', 36, '1', '习近平新时代中国特色社会主义思想，明确必须坚持和完善社会主义基本经济制度，使市场在资源配置中起决定性作用，更好发挥政府作用，把握新发展阶段，贯彻创新、协调、绿色、开放、共享的新发展理念，加快构建以国内大循环为主体、国内国际双循环相互促进的新发展格局，推动_发展，统筹发展和安全。()', '[\"A:高质量\", \"B:高速度\", \"C:高水平\", \"D:高效率\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1715, NULL, 'single-choice', 37, '1', '党的十九大报告明确，全面深化改革总目标是完善和发展中国特色社会主义制度、推进国家现代化。()。', '[\"A:治理体系\", \"B:治理能力\", \"C:治理体系和治理能力\", \"D:治理体制和治理能力\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1716, NULL, 'single-choice', 38, '1', '中国特色社会主义事业总体布局是 战略布局是_____。', '[\"A:“四位一体”；“四个全面”\", \"B:“五位一体”；“四个全面”\", \"C:“五位一体”；“五个全面”\", \"D:“六位一体”；“五个全面”\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1717, NULL, 'single-choice', 39, '1', '改革开放以来，我们党全部理论和实践的鲜明主题是()，也是习近平新时代中国特色社会主义思想的核心要义', '[\"A:什么是社会主义，怎样建设社会主义\", \"B:建设一个什么样的党，怎样建设党\", \"C:实现什么样的发展，怎样发展\", \"D:坚持和发展中国特色社会主义\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1718, NULL, 'single-choice', 40, '1', '习近平新时代中国特色社会主义思想，明确中国特色大国外交要服务_、促进人类进步，推动建设新型国际关系，推动构建人类命运共同体。()', '[\"A:民族复兴\", \"B:祖国强大\", \"C:国际竞争\", \"D:强国战略\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1719, NULL, 'multiple-choice', 15, '1', '(多选）习近平新时代中国特色社会主义思想：', '[\"A:是当代中国马克思主义、二十一世纪马克思主义\", \"B:是中华文化和中国精神的时代精华\", \"C:实现了马克思主义中国化新的飞跃\", \"D:是马克思主义中国化第一次飞跃的成果\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1720, NULL, 'multiple-choice', 16, '1', '(多选）习近平新时代中国特色社会主义思想的历史地位', '[\"A:马克思主义中国化新的飞跃\", \"B:中华文化和中国精神的时代精华\", \"C:当代中国的马克思主义、二十一世纪马克主义\", \"D:人类社会发展的科学指南\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1721, NULL, 'multiple-choice', 17, '1', '（多选）习近平新时代中国特色社会主义思想的丰富内涵，其主要内容包括()', '[\"A:“十三个方面成就”\", \"B:“两个基本点”\", \"C:“十个明确”\", \"D:“十四个坚持”\"]', 'CD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1722, NULL, 'multiple-choice', 18, '1', '（多选）党的十九大明确了坚持和发展中国特色社会主义总任务，包括()', '[\"A:实现社会主义现代化\", \"B:实现中华民族伟大复兴\", \"C:实现社会主义的工业化\", \"D:分两步走在本世纪中叶建成富强民主文明和谐美丽的社会主义现代化强国\"]', 'ABD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1723, NULL, 'multiple-choice', 19, '1', '(多选）中国共产党人的精神支柱和政治灵魂是()，也是保持党的团结统一的思想基础。', '[\"A:共产主义远大理想\", \"B:中国特色社会主义共同理想\", \"C:共产主义崇高理想\", \"D:新时代中国特色社会主义共同思想\"]', 'AB', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1724, NULL, 'multiple-choice', 20, '1', '(多选）2019年5月21日，习近平在推动中部地区崛起工作座谈会上的讲话中说：“我经常讲，领导干部要胸怀两个大局，一个是___，一个是____，这是我们谋划工作的基本出发点。”', '[\"A:中国发展大局\", \"B:中华民族伟大复兴的战略全局\", \"C:世界格局\", \"D:世界百年未有之大变局\"]', 'BD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1725, NULL, 'multiple-choice', 21, '1', '(多选）“两个确立”是指()', '[\"A:确立习近平同志党中央的核心、全党的核心地\", \"B:确立习近平新时代中国特色社会主义思想的指导地位\", \"C:确立马克思主义的指导地位\", \"D:确立党的领导地位\"]', 'AB', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1726, NULL, 'multiple-choice', 22, '1', '.(多选)习近平新时代中国特色社会主义思想回答的重大时代课题有()', '[\"A:新时代坚持和发展什么样的中国特色社会主义、怎样坚持和发展中国特色社会主义\", \"B:建设什么样的社会主义现代化强国、怎样建设社会主义现代化强国\", \"C:建设什么样的长期执政的马克思主义政党、怎样建设长期执政的马克思主义政党\", \"D:什么是社会主义，怎样建设社会主义\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1727, NULL, 'multiple-choice', 23, '1', '(多选)在中国共产党第二十次全国代表大会上的报告中，习近平指出：“中国共产党人深刻认识到，只有把马克思主义基本原理同______相结合、同_', '[\"A:中国具体实际\", \"B:中华优秀传统文化\", \"C:时代特征\", \"D:中国精神\"]', 'AB', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1728, NULL, 'single-choice', 41, '1', '在当今中国，没有大于中国共产党的政治力量或其他什么力量。党政军民学，东西南北中，党是领导一切的，是最高的()。', '[\"A:思想领导力量\", \"B:经济领导力量\", \"C:政治领导力量\", \"D:科技领导力量\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1729, NULL, 'single-choice', 42, '1', '中国特色社会主义制度是一个严密完整的科学制度体系，起四梁八柱作用的是根本制度、基本制度、重要制度，其中具有统领地位的是()。', '[\"A:重要制度\", \"B:党的领导制度\", \"C:基本制度\", \"D:根本制度\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1730, NULL, 'single-choice', 43, '1', '我国社会主义政治制度优越性的一个突出特点是党总揽全局、协调各方的领导核心作用，形象地说是“众星捧月”，这个“月”就是()。', '[\"A:政府\", \"B:人大\", \"C:中国共产党\", \"D:检察机关\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1731, NULL, 'single-choice', 44, '1', '在国家治理体系的大棋局中，()是坐镇中军帐的“帅”，车马炮各展其长，一盘棋大局分明。', '[\"A:国务院\", \"B:人大\", \"C:政协\", \"D:党中央\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1732, NULL, 'multiple-choice', 24, '1', '（多选）中国共产党是中国特色社会主义的领导核心，是社会主义现代化建设的根本保证。有了中国共产党的领导()。', '[\"A:才能制定和执行正确的路线方针和政策\", \"B:才能坚持中国现代化建设的正确方向\", \"C:才能维护国家统一、社会和谐稳定，应对复杂国际环境\", \"D:才能正确处理各种矛盾，凝聚亿万人民力量\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1733, NULL, 'multiple-choice', 25, '1', '（多选）中国共产党领导是中国特色社会主义制度的最大优势。这是因为()。', '[\"A:中国共产党是中国特色社会主义制度的创建者\", \"B:中国共产党的领导是充分发挥中国特色社会主义制度优势的根本保障\", \"C:中国共产党的自身优势是中国特色社会主义制度优势的主要来源\", \"D:中国共产党是最高政治领导力量\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1734, NULL, 'multiple-choice', 26, '1', '（多选）中国共产党作为最高政治领导力量，不是抽象的而是具体的，主要体现在如哪些方面？()', '[\"A:统领政治体系\", \"B:决策重大问题\", \"C:主导社会治理\", \"D:把准政治方向\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1735, NULL, 'multiple-choice', 27, '1', '(多选）党是最高的政治领导力量，对这句话理解正确的是()。', '[\"A:这是马克思主义政党学说的基本原则\", \"B:这是对历史经验的深刻总结\", \"C:归根到底是由近代以来中国发展的历史逻辑、理论逻辑、实践逻辑决定的\", \"D:这是推进伟大事业的根本保证\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1736, NULL, 'multiple-choice', 28, '1', '(多选）大局意识就是要()', '[\"A:确保中央政令畅通\", \"B:正确处理中央与地方、局部与全局、当前与长远的关系\", \"C:自觉从党和国家大局出发想问题、办事情、抓落实\", \"D:坚决贯彻落实中央决策部署\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1737, NULL, 'multiple-choice', 29, '1', '.(多选）党的领导是全面的。关于这一句的理解正确的是(', '[\"A:领导方法要全面\", \"B:领导内容要全面\", \"C:领导过程要全面\", \"D:领导对象要全面覆盖\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1738, NULL, 'multiple-choice', 30, '1', '.(多选）核心意识就是要()', '[\"A:更加自觉地在思想上政治上行动上同党中央保持高度一致\", \"B:更加紧密地团结在以习近平同志为核心的党中央周围\", \"C:特别是党中央的集中统一领导\", \"D:更加坚定地维护党中央权威\", \"E:始终坚持、切实加强党的领导\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1739, NULL, 'multiple-choice', 31, '1', '(多选）“政治意识”表现为()', '[\"A:坚持政治原则\", \"B:站稳政治立场，保持政治清醒和政治定力\", \"C:坚持正确的政治方向\", \"D:坚定政治信仰\", \"E:不断提高政治判断力、政治领悟力、政治执行力\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1740, NULL, 'multiple-choice', 32, '1', '(多选）看齐意识就是要求()', '[\"A:向党中央决策部署看齐\", \"B:向党的理论和路线方针政策看齐\", \"C:向党中央看齐\", \"D:做到党中央提倡的坚决响应、党中央决定的坚决执行、党中央禁止的坚决不做\"]', 'ABCD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1741, NULL, 'single-choice', 45, '1', '2021年7月1日，习近平在庆祝中国共产党成立100周年大会上的讲话中指出，为了实现中华民族伟大复兴，中国共产党团结带领中国人民，自信自强、守正创新，统揽伟大斗争、伟大工程、伟大事业、伟大梦想，创造了新时代中国特色社会主义的伟大成就。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告：中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复兴进入了()历史进程！', '[\"A:最好的\", \"B:快速发展的\", \"C:新的\", \"D:不可逆转的\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1742, NULL, 'single-choice', 46, '1', '要实现中华民族伟大复兴的中国梦，必须弘扬()为核心的民族精神', '[\"A:以人为本\", \"B:爱国主义\", \"C:改革创新\", \"D:社会主义核心价值观\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1743, NULL, 'single-choice', 47, '1', '2012年11月29日，习近平率中央政治局常委和中央书记处的同志来到国家博物馆，参观复兴之路展览，明确提出：近代以来中华民族最伟大的梦想是()', '[\"A:实现中华民族伟大复兴\", \"B:实现国家富强\", \"C:实现中华民族伟大崛起\", \"D:全面建成小康社会\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1744, NULL, 'single-choice', 48, '1', '我们党原来提出的“三步走”战略的第三步即基本实现现代化，将提前15年，即在()实现', '[\"A:2020年\", \"B:2025年\", \"C:2030年\", \"D:2035年\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1745, NULL, 'single-choice', 49, '1', '社会主义革命和建设时期，中国共产党团结带领中国人民，自力更生、发愤图强，创造了社会主义革命和建设的伟大成就，为实现中华民族伟大复兴()。', '[\"A:创造了根本社会条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1746, NULL, 'single-choice', 50, '1', '从全面建成小康社会到基本实现现代化，再到全面建成____，是新时代中国特色社会主义发展的战略安排。', '[\"A:创新型国家\", \"B:世界一流强国\", \"C:社会主义现代化大国\", \"D:社会主义现代化强国\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1747, NULL, 'single-choice', 51, '1', '要实现中华民族伟大复兴的中国梦，必须弘扬()为核心的时代精神', '[\"A:以人为本\", \"B:爱国主义\", \"C:改革创新\", \"D:社会主义核心价值观\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1748, NULL, 'single-choice', 52, '1', '《中共中央关于党的百年奋斗重大成就和历史经验的决议》提出，党的十八大以来，以习近平同志为核心的党中央领导全党全军全国各族人民砥砺前行，全面建成小康社会目标如期实现，党和国家事业取得历史性成就、发生历史性变革，彰显了中国特色社会主义的强大生机活力，党心军心民心空前凝聚振奋，为实现中华民族伟大复兴提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量。中国共产党和中国人民以英勇顽强的奋斗向世界庄严宣告，中华民族迎来了()。', '[\"A:中国从几千年封建专制政治向人民民主的伟大飞跃\", \"B:一穷二白、人口众多的东方大国大步迈进社会主义社会的伟大飞跃\", \"C:中华民族从站起来到富起来的伟大飞跃\", \"D:从站起来、富起来到强起来的伟大飞跃\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1749, NULL, 'single-choice', 53, '1', '新民主主义革命时期，中国共产党团结带领中国人民，浴血奋战、百折不挠，创造了新民主主义革命的伟大成就，为实现中华民族伟大复兴()。', '[\"A:创造了根本社会条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1750, NULL, 'single-choice', 54, '1', '《中共中央关于党的百年奋斗重大成就和历史经验的决议》提出，经过二十八年浴血奋斗，党领导人民，在各民主党派和无党派民主人士积极合作下，于一九四九年十月一日宣告成立中华人民共和国，实现民族独立、人民解放，彻底结束了旧中国半殖民地半封建社会的历史，彻底结束了极少数剥削者统治广大劳动人民的历史，彻底结束了旧中国一盘散沙的局面，彻底废除了列强强加给中国的不平等条约和帝国主义在中国的一切特权，实现了()，也极大改变了世界政治格局，鼓舞了全世界被压迫民族和被压迫人民争取解放的斗争。', '[\"A:中国从几千年封建专制政治向人民民主的伟大飞跃\", \"B:一穷二白、人口众多的东方大国大步迈进社会主义社会的伟大飞跃\", \"C:中华民族从站起来到富起来的伟大飞跃\", \"D:从站起来、富起来到强起来的伟大飞跃\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1751, NULL, 'single-choice', 55, '1', '《中共中央关于党的百年奋斗重大成就和历史经验的决议》提出，从新中国成立到改革开放前夕，党领导人民完成社会主义革命，消灭一切剥削制度，实现了中华民族有史以来最为广泛而深刻的社会变革，实现了()。', '[\"A:中国从几千年封建专制政治向人民民主的伟大飞跃\", \"B:一穷二白、人口众多的东方大国大步迈进社会主义社会的伟大飞跃\", \"C:中华民族从站起来到富起来的伟大飞跃\", \"D:从站起来、富起来到强起来的伟大飞跃\"]', 'B', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1752, NULL, 'single-choice', 56, '1', '《中共中央关于党的百年奋斗重大成就和历史经验的决议》提出，改革开放和社会主义现代化建设的伟大成就举世瞩目，我国实现了从生产力相对落后的状况到经济总量跃居世界第二的历史性突破，实现了人民生活从温饱不足到总体小康、奔向全面小康的历史性跨越，推进了()。', '[\"A:中国从几千年封建专制政治向人民民主的伟大飞跃\", \"B:一穷二白、人口众多的东方大国大步迈进社会主义社会的伟大飞跃\", \"C:中华民族从站起来到富起来的伟大飞跃\", \"D:从站起来、富起来到强起来的伟大飞跃\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1753, NULL, 'single-choice', 57, '1', '中国特色社会主义新时代，中国共产党团结带领中国人民，自信自强、守正创新，统揽伟大斗争、伟大工程、伟大事业、伟大梦想，创造了新时代中国特色社会主义的伟大成就，为实现中华民族伟大复兴()。', '[\"A:创造了根本社会条件\", \"B:奠定了根本政治前提和制度基础\", \"C:提供了充满新的活力的体制保证和快速发展的物质条件\", \"D:提供了更为完善的制度保证、更为坚实的物质基础、更为主动的精神力量\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1754, NULL, 'single-choice', 58, '1', '2021年7月1日，习近平总书记在庆祝中国共产党成立100周年大会上庄严宣告：“经过全党全国各族人民持续奋斗，我们实现了第一个百年奋斗目标，在中华大地上()，历史性地解决了绝对贫困问题，正在意气风发向着全面建成社会主义现代化强国的第二个百年奋斗目标迈进。”', '[\"A:全面建成了小康社会\", \"B:全面建设小康社会\", \"C:全面成社会主义强国\", \"D:基本实现了社会主义现代化\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1755, NULL, 'single-choice', 59, '1', '《中共中央关于党的百年奋斗重大成就和历史经验的决议》指出：()是中华民族精神之魂，是我们立党立国的重要原则。', '[\"A:爱国主义\", \"B:改革开放\", \"C:以人民为中心\", \"D:独立自主\"]', 'D', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1756, NULL, 'single-choice', 60, '1', '2021年7月1日，习近平在庆祝中国共产党成立100周年大会上的讲话中强调：()是党和人民历经千辛万苦、付出巨大代价取得的根本成就，是实现中华民族伟大复兴的正确道路。', '[\"A:中国特色社会主义\", \"B:实现中华民族伟大复兴\", \"C:解放和发展生产力\", \"D:共同富裕\"]', 'A', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1757, NULL, 'single-choice', 61, '1', '实现中国梦必须走中国道路，这就是()', '[\"A:改革开放的道路\", \"B:科学发展的道路\", \"C:中国特色社会主义道路\", \"D:生态文明的道路\"]', 'C', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1758, NULL, 'multiple-choice', 33, '1', '中国梦归根到底是人民的梦，人民是中国梦的主体，是中国梦的()', '[\"A:创造者\", \"B:见证者\", \"C:领导者\", \"D:享有者\"]', 'AD', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1759, NULL, 'multiple-choice', 34, '1', '科学社会主义基本原则不能丢，丢了就不是社会主义。下列属于科学社会主义基本原则的是：', '[\"A:无产阶级政党是无产阶级的先锋队，社会主义事业必须坚持无产阶级政党的领导\", \"B:无产阶级革命是无产阶级进行斗争的最高形式，必须以建立无产阶级专政的国家为目的\", \"C:在生产资料公有制基础上组织生产，满足全体社会成员的需要是社会主义生产的根本目的\", \"D:对社会生产进行有计划的指导和调节，实行等量劳动领取等量产品的按劳分配原则\", \"E:通过无产阶级专政和社会主义高度发展最终实现向消灭阶级、消灭剥削、实现人的全面而自由发展的共产主义社会的过渡\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1760, NULL, 'multiple-choice', 35, '1', '实现中国梦必须()', '[\"A:走中国道路\", \"B:弘扬中国精神\", \"C:凝聚中国力量\", \"D:传播中国文化\"]', 'ABC', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1761, NULL, 'multiple-choice', 36, '1', '2022年3月6日，习近平在参加全国政协十三届五次会议农业界、社会福利和社会保障界委员联组会时概括的“五个战略性有利条件”是：', '[\"A:有中国共产党的坚强领导\", \"B:有中国特色社会主义制度的显著优势\", \"C:有持续快速发展积累的坚实基础\", \"D:有长期稳定的社会环境\", \"E:有自信自强的精神力量\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1762, NULL, 'multiple-choice', 37, '1', '中国特色社会主义坚持了科学社会主义基本原则并赋予其鲜明中国特色，主要表现有：', '[\"A:在领导力量上，中国共产党领导是中国特色社会主义最本质的特征，是中国特色社会主义制度的最大优势，党是最高政治领导力量\", \"B:在国体和政体上，实行人民民主专政的国体和人民代表大会制度的政体\", \"C:在经济制度上，坚持公有制为主体、多种所有制经济共同发展，坚持按劳分配为主体、多种分配方式并存，实行社会主义市场经济体制\", \"D:在意识形态上，坚守马克思主义信仰、共产主义远大理想、中国特色社会主义共同理想\", \"E:根本立场上，坚持以人民为中心，不断促进人的全面发展，实现全体人民共同富裕\"]', 'ABCDE', NULL, 'medium', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1763, 10, 'choice', 1, 'TestSubject', 'Question by userA_1766811686', '[\"A\", \"B\"]', 'A', NULL, 'easy', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1764, 13, 'single-choice', 1, 'TestSubject', 'Exportable Question by userIE_1766811798', '[\"Option A\", \"Option B\", \"Option C\", \"Option D\"]', 'A', 'Test analysis', 'easy', 0, 0, 0, NULL, NULL);
INSERT INTO `question` VALUES (1765, 14, 'single-choice', 2, 'TestSubject', 'Practice Question by userPR_1766811840', '[\"A\", \"B\", \"C\", \"D\"]', 'A', NULL, 'easy', 0, 0, 0, NULL, NULL);

-- ----------------------------
-- Table structure for subject
-- ----------------------------
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '科目名称',
  `question_count` int NULL DEFAULT 0 COMMENT '题目数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '科目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subject
-- ----------------------------
INSERT INTO `subject` VALUES (1, '习思想', 98, NULL);
INSERT INTO `subject` VALUES (2, '密码学', 29, '2025-12-19 16:33:01');
INSERT INTO `subject` VALUES (3, '习近平思想', 0, NULL);
INSERT INTO `subject` VALUES (4, '1', 98, NULL);
INSERT INTO `subject` VALUES (5, '2', 0, NULL);
INSERT INTO `subject` VALUES (6, 'TestSubject', 3, NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色:user/admin',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态:0禁用/1启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '', NULL, 'admin', 1, '2025-12-27 11:22:11', '2025-12-27 14:34:52');
INSERT INTO `user` VALUES (2, 'tester', '$2a$10$KIHrX9.Of5MC7XkzlJi4X.vzs/s4e4VxNJKcS6AMg4fS4n9ysWEha', 'tester', NULL, NULL, 'user', 1, '2025-12-27 12:00:30', '2025-12-27 12:00:30');
INSERT INTO `user` VALUES (3, 'test1', '$2a$10$LUHoczLyvmQ4Xzdui/5WZORa6DW/b/u2FsWDccYABLZOkcs9wHCIi', 'test1', '1', NULL, 'user', 1, '2025-12-27 12:06:58', '2025-12-27 13:24:50');
INSERT INTO `user` VALUES (4, 'testuser_1766811533', '$2a$10$KDLUm4CDfsmx.KUquonVSu9d93rLVba8RmJxtunl2dReJwafbD9TK', 'Test User', NULL, NULL, 'user', 1, '2025-12-27 12:58:54', '2025-12-27 12:58:54');
INSERT INTO `user` VALUES (5, 'testuser_1766811556', '$2a$10$WxQKQgIvv/JHBY/m/CjiWuuPgf59UhhP8Xr.UVTb.HSPOStuKzioy', 'Test User', NULL, NULL, 'user', 1, '2025-12-27 12:59:16', '2025-12-27 12:59:16');
INSERT INTO `user` VALUES (6, 'testuser_1766811582', '$2a$10$zk1qAifHhqNV5/mRu89nVecor4hlK0JOyGs2/ni5VPVBiRkxLjx6q', 'Test User', NULL, NULL, 'user', 1, '2025-12-27 12:59:43', '2025-12-27 12:59:43');
INSERT INTO `user` VALUES (8, 'userA_1766811657', '$2a$10$ZiJ6JjIpwquRT2Y2xwLUCOhtjmbVsohZ1R9bfcCYuEZOFBM1Y/3xK', 'userA_1766811657', NULL, NULL, 'user', 1, '2025-12-27 13:00:58', '2025-12-27 13:42:35');
INSERT INTO `user` VALUES (10, 'userA_1766811686', '$2a$10$s5GKuFlTn3i/pMb0cnfcEutK3eP4xCFyJbACzcqFo68AoQ7fnEbu6', 'userA_1766811686', NULL, NULL, 'user', 1, '2025-12-27 13:01:26', '2025-12-27 13:01:26');
INSERT INTO `user` VALUES (11, 'userB_1766811686', '$2a$10$ZPSAyE1EgY52fiAf4HesOePckLeIuikmBEHhRMEW/uld4QlbHrL9W', 'userB_1766811686', NULL, NULL, 'user', 1, '2025-12-27 13:01:26', '2025-12-27 13:01:26');
INSERT INTO `user` VALUES (12, 'userP_1766811710', '$2a$10$8sexIUzX.xwFR.LXSKCUUebFpR.s2Yde1DqQ0QmX9Ml.g/CrcN4nS', 'userP_1766811710', NULL, NULL, 'user', 1, '2025-12-27 13:01:50', '2025-12-27 13:01:50');
INSERT INTO `user` VALUES (13, 'userIE_1766811798', '$2a$10$uEtEfrn3/BsKm5Er07g/GuZJW1xSs2M.24k6Rz6G4XEz7sgbkciva', 'userIE_1766811798', NULL, NULL, 'user', 1, '2025-12-27 13:03:18', '2025-12-27 13:03:18');
INSERT INTO `user` VALUES (14, 'userPR_1766811840', '$2a$10$DgviPKdgAxTNBzv9BZap6OACNxUY6RKNLnRWZB77C2xGOfmQ5sOEq', 'userPR_1766811840', NULL, NULL, 'user', 1, '2025-12-27 13:04:01', '2025-12-27 13:04:01');
INSERT INTO `user` VALUES (15, 'userSB_1766811873', '$2a$10$pH3m1ZB9r.Q8nVb7KYw1tuERpH5/zn3cW0UjihB8bsd8nm5vMYPI2', 'userSB_1766811873', NULL, NULL, 'user', 1, '2025-12-27 13:04:34', '2025-12-27 13:04:34');

-- ----------------------------
-- Table structure for user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'User-Agent',
  `login_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '登录状态:0失败/1成功',
  `fail_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_login_ip`(`login_ip` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_login_log
-- ----------------------------
INSERT INTO `user_login_log` VALUES (1, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '密码错误', '2025-12-27 11:56:53');
INSERT INTO `user_login_log` VALUES (2, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '密码错误', '2025-12-27 11:58:38');
INSERT INTO `user_login_log` VALUES (3, 2, 'tester', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 12:01:56');
INSERT INTO `user_login_log` VALUES (4, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '密码错误', '2025-12-27 12:03:10');
INSERT INTO `user_login_log` VALUES (5, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 12:03:13');
INSERT INTO `user_login_log` VALUES (6, NULL, 'test', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '用户不存在', '2025-12-27 12:03:58');
INSERT INTO `user_login_log` VALUES (7, 3, 'test1', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 12:07:06');
INSERT INTO `user_login_log` VALUES (8, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '密码错误', '2025-12-27 12:18:53');
INSERT INTO `user_login_log` VALUES (9, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 0, '密码错误', '2025-12-27 12:19:28');
INSERT INTO `user_login_log` VALUES (10, 4, 'testuser_1766811533', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 12:58:54');
INSERT INTO `user_login_log` VALUES (11, 5, 'testuser_1766811556', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 12:59:16');
INSERT INTO `user_login_log` VALUES (12, 6, 'testuser_1766811582', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 12:59:43');
INSERT INTO `user_login_log` VALUES (13, 7, 'testuser_1766811616', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:00:16');
INSERT INTO `user_login_log` VALUES (14, 8, 'userA_1766811657', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:00:58');
INSERT INTO `user_login_log` VALUES (15, 9, 'userB_1766811658', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:00:58');
INSERT INTO `user_login_log` VALUES (16, 10, 'userA_1766811686', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:01:26');
INSERT INTO `user_login_log` VALUES (17, 11, 'userB_1766811686', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:01:26');
INSERT INTO `user_login_log` VALUES (18, 12, 'userP_1766811710', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:01:50');
INSERT INTO `user_login_log` VALUES (19, 13, 'userIE_1766811798', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:03:18');
INSERT INTO `user_login_log` VALUES (20, 14, 'userPR_1766811840', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:04:01');
INSERT INTO `user_login_log` VALUES (21, 15, 'userSB_1766811873', '0:0:0:0:0:0:0:1', NULL, 'Unknown', 'Unknown', 'python-requests/2.31.0', 1, NULL, '2025-12-27 13:04:34');
INSERT INTO `user_login_log` VALUES (22, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 13:23:43');
INSERT INTO `user_login_log` VALUES (23, 3, 'test1', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 13:24:35');
INSERT INTO `user_login_log` VALUES (24, 1, 'admin', '0:0:0:0:0:0:0:1', NULL, 'Chrome', 'Windows', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36', 1, NULL, '2025-12-27 13:27:28');

-- ----------------------------
-- Table structure for user_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `user_operation_log`;
CREATE TABLE `user_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `operation_data` json NULL COMMENT '操作数据',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求IP',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求参数',
  `cost_time` bigint NULL DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `operation_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '操作状态',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_operation_log
-- ----------------------------
INSERT INTO `user_operation_log` VALUES (1, 13, 'userIE_1766811798', 'IMPORT', 'Excel批量导入题目', NULL, '0:0:0:0:0:0:0:1', '/api/import/excel', '[2 params]', 50, 'POST', 1, NULL, '2025-12-27 13:03:19');

-- ----------------------------
-- Table structure for user_question_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_question_stats`;
CREATE TABLE `user_question_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `practice_count` int NOT NULL DEFAULT 0 COMMENT '练习次数',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '错误次数',
  `is_marked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
  `last_practice_time` datetime NULL DEFAULT NULL COMMENT '最后练习时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_wrong_count`(`wrong_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户题目统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_question_stats
-- ----------------------------
INSERT INTO `user_question_stats` VALUES (1, 14, 1666, 1, 0, 0, '2025-12-27 13:04:01', '2025-12-27 13:04:01');

-- ----------------------------
-- Triggers structure for table question
-- ----------------------------
DROP TRIGGER IF EXISTS `before_question_insert`;
delimiter ;;
CREATE TRIGGER `before_question_insert` BEFORE INSERT ON `question` FOR EACH ROW BEGIN
    DECLARE max_order INT;
    -- 获取该科目+该类型的最大序号
    SELECT IFNULL(MAX(display_order), 0) INTO max_order FROM question WHERE type = NEW.type AND subject = NEW.subject;
    -- 设置新题目的序号
    SET NEW.display_order = max_order + 1;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
