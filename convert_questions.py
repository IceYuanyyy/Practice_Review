import re
import os

def clean_text(text):
    # Remove all asterisks using regex
    text = re.sub(r'\*+', '', text)
    
    # Fix known typos/formatting issues identified in analysis
    text = text.replace("答案解机：", "答案解析：")
    text = text.replace("正确答案：", "答案：")
    
    return text.strip()

def process_file(input_path):
    print(f"Processing {input_path} (Overwriting)")
    
    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except UnicodeDecodeError:
        print("UTF-8 decode failed, trying gbk...")
        with open(input_path, 'r', encoding='gbk') as f:
            lines = f.readlines()
            
    with open(input_path, 'w', encoding='utf-8') as out_f:
        for line in lines:
            cleaned = clean_text(line)
            if cleaned: # Skip empty lines after stripping
                out_f.write(cleaned + "\n")
            else:
                 if not line.strip():
                     out_f.write("\n")

if __name__ == "__main__":
    base_dir = r"f:\Development\Java\IDEA_Projects\Final_Practice"
    input_file = os.path.join(base_dir, "鸿蒙.txt")
    
    process_file(input_file)
    print("Done.")
