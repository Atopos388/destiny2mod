import re
import os
import json

# ==========================================
# 配置文件定义
# ==========================================
INPUT_FILE = "update.txt"
CONFIG_FILE = "logic_anchors.json"

# 内置的“出厂设置”逻辑锚点：哪怕第一次运行，也会检查这些核心功能
DEFAULT_ANCHORS = {
    "src/main/java/com/Atopos/destiny2mod/util/IgniteHandler.java": [
        "if (next >= 10)", 
        "apply(e, 5, source)"
    ],
    "src/main/java/com/Atopos/destiny2mod/entity/custom/SolarSnapProjectile.java": [
        "IgniteHandler.apply(livingTarget, 3",
        "isEnhanced",
        "target.invulnerableTime = 0"
    ],
    "src/main/java/com/Atopos/destiny2mod/event/ModEvents.java": [
        "OverloadActive",
        "OverloadKillWindow"
    ]
}

def load_anchors():
    """从本地记忆库加载逻辑锚点"""
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except:
            return DEFAULT_ANCHORS
    return DEFAULT_ANCHORS

def save_anchors(anchors):
    """保存记忆库到本地 JSON 文件"""
    with open(CONFIG_FILE, "w", encoding="utf-8") as f:
        json.dump(anchors, f, indent=4, ensure_ascii=False)

def extract_anchors_from_code(code):
    """从 Java 代码中自动提取 // @Anchor: \"内容\" 标记"""
    pattern = re.compile(r'//\s*@Anchor:\s*"([^"]+)"')
    return pattern.findall(code)

def apply_changes():
    """核心导入逻辑"""
    anchors_map = load_anchors()
    
    if not os.path.exists(INPUT_FILE):
        print(f"[-] 错误: 找不到 {INPUT_FILE}。请先创建它并粘贴 AI 的回复内容。")
        return

    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    # 正则表达式：精准匹配我发给你的代码块格式
    # 注意：确保这一行在你的编辑器里没有被折断
    pattern = re.compile(r"