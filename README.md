
# 📄 Resume Shortlister

## 📌 Overview

The **Resume Shortlister** is a tool designed to **automatically filter and shortlist resumes** based on job descriptions and required skill sets.
It helps recruiters, HR teams, and hiring managers quickly identify the most relevant candidates without manually going through every resume.

This project demonstrates how **text preprocessing, keyword matching, and scoring** can be applied to recruitment automation.

---

## 🚀 Features

* **Upload / Input Resume Data** (multiple resumes supported)
* **Parse & Extract Skills** from resumes (technical + soft skills)
* **Match Against Job Description**
* **Generate Shortlist Score** for each candidate
* **Rank Resumes** by relevance
* **Export Results** to a structured format (CSV/Excel/Text)

---

## 🛠️ Tech Stack

* **Programming Language**: Python (or Java, depending on your version)
* **Libraries / Tools** (Python version):

  * `pandas` – data handling
  * `nltk / spacy` – NLP & text processing
  * `sklearn` – vectorization & similarity scoring
* **Approach**:

  * Keyword extraction
  * TF-IDF / Cosine similarity scoring
  * Rule-based filtering (for must-have skills)

---

## 📂 Project Structure

```
ResumeShortlister/
│
├── resumes/                 # Folder containing resumes (txt/pdf/docx)
├── job_description.txt      # File with required job description
├── shortlist.py             # Main script (for scoring & ranking)
├── utils/                   # Helper functions (parser, text cleaner, etc.)
└── output/                  # Shortlisted candidates & reports
```

---

## ▶️ How to Run

1. Clone or download the project.
2. Install dependencies:

   ```bash
   pip install pandas scikit-learn nltk spacy
   ```

   *(or Java dependencies if running a Java version)*
3. Place resumes inside the `resumes/` folder.
4. Define the **job description** inside `job_description.txt`.
5. Run the program:

   ```bash
   python shortlist.py
   ```
6. View results inside the `output/` folder (CSV/Excel).

---

## 📸 App Flow

1. **Input Job Description**
2. **Upload/Load Resumes**
3. **Extract Skills & Keywords**
4. **Match & Score**
5. **Shortlist Candidates**
6. **Export Final Results**

---

## 💡 Future Enhancements

* Add **ATS (Applicant Tracking System) compatibility**.
* Use **LLMs (like GPT)** for smarter skill extraction.
* Add a **web interface** for non-technical users.
* Support **multi-criteria ranking** (education, experience, skills).
* Add **visual dashboards** for recruiters.



Do you want me to keep this README **general-purpose** (for both Python/Java versions), or should I make it **specific to your implementation** (e.g., only Python or only Java)?
