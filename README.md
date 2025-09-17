

# 📄 Advanced Resume Shortlister (Java)

## 📌 Overview

The **Advanced Resume Shortlister** is a **Java-based program** that automates the shortlisting of resumes against job requirements.
It parses resumes, extracts features such as **skills, experience, education, certifications, and projects**, and then ranks candidates using a **weighted scoring system**.

This project demonstrates **multi-threaded resume parsing, text extraction, regex-based analysis, and weighted scoring logic** for recruitment automation.

---

## 🚀 Features

* **Multi-threaded resume processing** for fast performance.
* Extracts and evaluates:

  * ✅ Technical Skills
  * ✅ Soft Skills
  * ✅ Education (degrees)
  * ✅ Certifications
  * ✅ Projects & Keywords
* **Weighted scoring system** with configurable weights.
* **Ranks candidates** by score.
* **Interactive mode**: view detailed candidate analysis in the console.
* **CSV Export** of ranked results for further review.

---

## 🛠️ Tech Stack

* **Java 11+**
* **Core Java Features**: Regex, Concurrency (ExecutorService), Streams
* **File I/O**: Reads resumes, job requirements, skills lists
* **CSV Export** for results
* *(Optional)*: PDF text extraction placeholder (can be extended with Apache PDFBox)

---

## 📂 Project Structure

```
AdvancedResumeShortlister.java   # Main application file
technical_skills.txt             # List of technical skills
soft_skills.txt                  # List of soft skills
job_requirements.txt             # Job requirements + weights
resumes/                         # Folder containing resumes (.txt/.pdf)
    candidate1.txt
    candidate2.pdf
shortlist_results.csv            # Output file (generated after run)
```

---

## 📄 File Formats

### 1. Job Requirements (`job_requirements.txt`)

Define required skills and weights:

```
SKILL: Java:5
SKILL: Python:4
SKILL: Machine Learning:5
DEGREE: Master
CERT: AWS Certified
KEYWORD: NLP
WEIGHT_EXPERIENCE: 1.0
WEIGHT_EDUCATION: 0.8
WEIGHT_CERTIFICATION: 0.6
WEIGHT_PROJECT: 0.5
```

### 2. Technical Skills (`technical_skills.txt`)

```
Java
Python
C++
Machine Learning
Data Science
```

### 3. Soft Skills (`soft_skills.txt`)

```
Leadership
Teamwork
Problem Solving
Adaptability
```

### 4. Resumes (`/resumes/`)

* Resumes can be **.txt** or **.pdf** files.
* Example:

```
resumes/
    candidate1.txt
    candidate2.pdf
    candidate3.txt
```

---

## ▶️ How to Run

1. Ensure **Java JDK 11+** is installed.
2. Place `AdvancedResumeShortlister.java` in your project directory.
3. Place input files (`job_requirements.txt`, `technical_skills.txt`, `soft_skills.txt`) in the same folder.
4. Create a `resumes/` folder and add resumes.
5. Compile the project:

   ```bash
   javac AdvancedResumeShortlister.java
   ```
6. Run the program:

   ```bash
   java AdvancedResumeShortlister job_requirements.txt ./resumes/
   ```

---

## 📊 Output

### Console (Top 10 Candidates)

```
Top Candidates:
Rank    Name            Score   Summary
----------------------------------------
1       candidate1.txt  42.5    Java, Python, AWS...
2       candidate2.pdf  38.0    Python, ML...
```

### Detailed Analysis (Interactive)

You can enter a candidate number to view detailed analysis:

```
Detailed Analysis for: candidate1.txt
Score: 42.5
Skills Matching:
- Java: 3 mentions
- Python: 2 mentions
Experience: 7 years
Education:
- Master of Computer Science
Certifications:
- AWS Certified
```

### CSV Export (`shortlist_results.csv`)

```
Rank,Name,Score,Top Skills,Experience,Education
1,candidate1.txt,42.5,Java;Python;AWS,7,Master of Computer Science
2,candidate2.pdf,38.0,Python;ML,5,Bachelor of Engineering
```

---

## 💡 Future Enhancements

* Integrate **Apache PDFBox** for real PDF text extraction.
* Use **NLP techniques** for smarter skill/project extraction.
* Add **GUI or Web Dashboard** for non-technical users.
* Extend to integrate with **ATS (Applicant Tracking Systems)**.



👉 Do you want me to also include a **ready-to-use sample job\_requirements.txt, technical\_skills.txt, and soft\_skills.txt** in the repo so you (or others) can run it immediately without creating files manually?
