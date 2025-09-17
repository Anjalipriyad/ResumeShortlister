# AdvancedResumeShortlister  

`AdvancedResumeShortlister` is a Java-based program that automatically analyzes and ranks resumes against a given job’s requirements. It extracts features like **skills, experience, education, certifications, and projects**, then calculates a weighted score for each candidate to generate a shortlist.  

---

## 🚀 Features  
- Multi-threaded resume parsing (fast parallel processing).  
- Extracts and matches **technical skills**, **soft skills**, **degrees**, **certifications**, and **projects**.  
- Weighted scoring system with configurable weights for each category.  
- Ranks candidates and provides both **summary and detailed analyses**.  
- Exports results into a `.csv` file for review.  

---

## ⚙️ Requirements  
- Java 11 or higher  
- A folder containing your resumes (`.txt` or `.pdf`)  
- A **job requirements file** with weights and preferences  
- A **technical skills list** and a **soft skills list** (plain text files, one skill per line)  

---

## 📂 Project Setup  

Organize your project like this:  
AdvancedResumeShortlister.java
job_requirements.txt
technical_skills.txt
soft_skills.txt
resumes/
resume1.txt
resume2.pdf
resume3.txt
