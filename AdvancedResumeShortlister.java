import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

public class AdvancedResumeShortlister {
    
    // Configuration constants
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int MAX_SKILL_MENTIONS = 5;
    private static final int MAX_EXPERIENCE_YEARS = 15;
    private static final double EXPERIENCE_WEIGHT = 0.7;
    private static final double SKILL_WEIGHT = 1.2;
    private static final double EDUCATION_WEIGHT = 0.5;
    
    // Knowledge bases (could be loaded from files/databases)
    private static final Set<String> TECHNICAL_SKILLS = loadSkills("technical_skills.txt");
    private static final Set<String> SOFT_SKILLS = loadSkills("soft_skills.txt");
    private static final Set<String> DEGREE_TYPES = Set.of(
        "BS", "B.S.", "Bachelor", "MS", "M.S.", "Master", 
        "PhD", "Ph.D", "Doctorate", "MBA"
    );
    private static final Set<String> TECH_CERTIFICATIONS = Set.of(
        "AWS Certified", "Oracle Certified", "CISSP", "PMP", 
        "Google Cloud Certified", "Microsoft Certified"
    );
    
    // Regular expression patterns
    private static final Pattern SKILL_PATTERN = buildSkillPattern();
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
        "(\\d+)\\s*\\+?\\s*years?\\s*(?:of\\s*)?experience", 
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EDUCATION_PATTERN = buildEducationPattern();
    private static final Pattern CERTIFICATION_PATTERN = buildCertificationPattern();
    private static final Pattern PROJECT_PATTERN = Pattern.compile(
        "project\\s*:\\s*(.+?)(?=\\n|$)", Pattern.CASE_INSENSITIVE
    );
    
    // Job requirements (would typically be loaded per position)
    private JobRequirements currentJobRequirements;
    
    public static void main(String[] args) {
        System.out.println("Arguments received: " + Arrays.toString(args));
        if (args.length < 2) {
            System.out.println("Usage: java AdvancedResumeShortlister <job_requirements_file> <resumes_directory>");
            return;
        }
        
        AdvancedResumeShortlister shortlister = new AdvancedResumeShortlister();
        
        try {
            // Load job requirements
            JobRequirements requirements = shortlister.loadJobRequirements(args[0]);
            shortlister.setCurrentJobRequirements(requirements);
            
            // Process resumes
            List<Resume> resumes = shortlister.loadResumesFromDirectory(args[1]);
            List<RankedResume> rankedResumes = shortlister.processResumes(resumes);
            
            // Display results
            shortlister.displayResults(rankedResumes);
            
            // Optional: Save results to file
            shortlister.saveResultsToFile(rankedResumes, "shortlist_results.csv");
            
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Core processing method
    public List<RankedResume> processResumes(List<Resume> resumes) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<RankedResume>> futures = new ArrayList<>();
        
        for (Resume resume : resumes) {
            futures.add(executor.submit(() -> processSingleResume(resume)));
        }
        
        List<RankedResume> results = new ArrayList<>();
        for (Future<RankedResume> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing resume: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        
        // Sort by score descending
        results.sort(Comparator.comparingDouble(RankedResume::getScore).reversed());
        
        return results;
    }
    
    private RankedResume processSingleResume(Resume resume) {
        // Extract all features
        ResumeFeatures features = extractFeatures(resume);
        
        // Calculate score
        double score = calculateScore(features);
        
        // Create detailed analysis
        String analysis = generateAnalysis(features);
        
        return new RankedResume(resume, features, score, analysis);
    }
    
    private ResumeFeatures extractFeatures(Resume resume) {
        String content = resume.getContent();
        
        // Extract skills with context
        Map<String, List<String>> skillContexts = new HashMap<>();
        Matcher skillMatcher = SKILL_PATTERN.matcher(content);
        while (skillMatcher.find()) {
            String skill = skillMatcher.group(1);
            String context = extractContext(content, skillMatcher.start(), 50);
            skillContexts.computeIfAbsent(skill, k -> new ArrayList<>()).add(context);
        }
        
        // Extract experience
        int yearsExperience = 0;
        Matcher expMatcher = EXPERIENCE_PATTERN.matcher(content);
        if (expMatcher.find()) {
            yearsExperience = Integer.parseInt(expMatcher.group(1));
        }
        
        // Extract education
        List<String> education = new ArrayList<>();
        Matcher eduMatcher = EDUCATION_PATTERN.matcher(content);
        while (eduMatcher.find()) {
            education.add(eduMatcher.group());
        }
        
        // Extract certifications
        List<String> certifications = new ArrayList<>();
        Matcher certMatcher = CERTIFICATION_PATTERN.matcher(content);
        while (certMatcher.find()) {
            certifications.add(certMatcher.group());
        }
        
        // Extract projects
        List<String> projects = new ArrayList<>();
        Matcher projectMatcher = PROJECT_PATTERN.matcher(content);
        while (projectMatcher.find()) {
            projects.add(projectMatcher.group(1));
        }
        
        return new ResumeFeatures(
            skillContexts,
            yearsExperience,
            education,
            certifications,
            projects
        );
    }
    
    private double calculateScore(ResumeFeatures features) {
        double score = 0;
        
        // Skill matching (weighted by job requirements)
        for (Map.Entry<String, Integer> req : currentJobRequirements.getSkillWeights().entrySet()) {
            String requiredSkill = req.getKey();
            int weight = req.getValue();
            
            if (features.getSkillContexts().containsKey(requiredSkill)) {
                int mentions = Math.min(features.getSkillContexts().get(requiredSkill).size(), MAX_SKILL_MENTIONS);
                score += mentions * weight * SKILL_WEIGHT;
            }
        }
        
        // Experience scoring
        int relevantExperience = Math.min(features.getYearsExperience(), MAX_EXPERIENCE_YEARS);
        score += relevantExperience * currentJobRequirements.getExperienceWeight() * EXPERIENCE_WEIGHT;
        
        // Education scoring
        for (String degree : features.getEducation()) {
            if (currentJobRequirements.getPreferredDegrees().stream().anyMatch(degree::contains)) {
                score += EDUCATION_WEIGHT * currentJobRequirements.getEducationWeight();
            }
        }
        
        // Certification bonus
        for (String cert : features.getCertifications()) {
            if (currentJobRequirements.getPreferredCertifications().contains(cert)) {
                score += currentJobRequirements.getCertificationWeight();
            }
        }
        
        // Project relevance
        for (String project : features.getProjects()) {
            if (containsAnyKeyword(project, currentJobRequirements.getKeywords())) {
                score += currentJobRequirements.getProjectWeight();
            }
        }
        
        return score;
    }
    
    private String generateAnalysis(ResumeFeatures features) {
        StringBuilder analysis = new StringBuilder();
        
        // Skills analysis
        analysis.append("Skills Matching:\n");
        for (String reqSkill : currentJobRequirements.getSkillWeights().keySet()) {
            if (features.getSkillContexts().containsKey(reqSkill)) {
                int count = features.getSkillContexts().get(reqSkill).size();
                analysis.append(String.format("- %s: %d mentions\n", reqSkill, count));
            }
        }
        
        // Experience analysis
        analysis.append(String.format("\nExperience: %d years\n", features.getYearsExperience()));
        
        // Education analysis
        if (!features.getEducation().isEmpty()) {
            analysis.append("\nEducation:\n");
            features.getEducation().forEach(edu -> analysis.append("- ").append(edu).append("\n"));
        }
        
        // Certifications analysis
        if (!features.getCertifications().isEmpty()) {
            analysis.append("\nCertifications:\n");
            features.getCertifications().forEach(cert -> analysis.append("- ").append(cert).append("\n"));
        }
        
        return analysis.toString();
    }
    
    // Helper methods
    private String extractContext(String text, int position, int windowSize) {
        int start = Math.max(0, position - windowSize);
        int end = Math.min(text.length(), position + windowSize);
        return text.substring(start, end).replaceAll("\\s+", " ").trim();
    }
    
    private boolean containsAnyKeyword(String text, Set<String> keywords) {
        return keywords.stream().anyMatch(keyword -> 
            Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE)
                   .matcher(text).find());
    }
    
    private static Pattern buildSkillPattern() {
        String technical = String.join("|", TECHNICAL_SKILLS);
        String soft = String.join("|", SOFT_SKILLS);
        return Pattern.compile("\\b(" + technical + "|" + soft + ")\\b", Pattern.CASE_INSENSITIVE);
    }
    
    private static Pattern buildEducationPattern() {
        String degrees = String.join("|", DEGREE_TYPES);
        return Pattern.compile(
            "(" + degrees + ")[\\s\\w]*?(in|of)?[\\s\\w]*?\\b(Computer Science|Engineering|IT|Information Technology)\\b",
            Pattern.CASE_INSENSITIVE
        );
    }
    
    private static Pattern buildCertificationPattern() {
        String certs = String.join("|", TECH_CERTIFICATIONS);
        return Pattern.compile("\\b(" + certs + ")[\\w\\s]*?\\b", Pattern.CASE_INSENSITIVE);
    }
    
    private static Set<String> loadSkills(String filename) {
        try {
            return Files.lines(Paths.get(filename))
                      .map(String::trim)
                      .filter(line -> !line.isEmpty())
                      .collect(Collectors.toSet());
        } catch (IOException e) {
            System.err.println("Warning: Could not load skills file " + filename + ", using defaults");
            return Collections.emptySet();
        }
    }
    
    // Data classes
    public static class Resume {
        private final String name;
        private final String content;
        
        public Resume(String name, String content) {
            this.name = name;
            this.content = content;
        }
        
        public String getName() { return name; }
        public String getContent() { return content; }
    }
    
    public static class RankedResume {
        private final Resume resume;
        private final ResumeFeatures features;
        private final double score;
        private final String analysis;
        
        public RankedResume(Resume resume, ResumeFeatures features, double score, String analysis) {
            this.resume = resume;
            this.features = features;
            this.score = score;
            this.analysis = analysis;
        }
        
        public Resume getResume() { return resume; }
        public ResumeFeatures getFeatures() { return features; }
        public double getScore() { return score; }
        public String getAnalysis() { return analysis; }
    }
    
    public static class ResumeFeatures {
        private final Map<String, List<String>> skillContexts;
        private final int yearsExperience;
        private final List<String> education;
        private final List<String> certifications;
        private final List<String> projects;
        
        public ResumeFeatures(Map<String, List<String>> skillContexts, int yearsExperience,
                            List<String> education, List<String> certifications,
                            List<String> projects) {
            this.skillContexts = skillContexts;
            this.yearsExperience = yearsExperience;
            this.education = education;
            this.certifications = certifications;
            this.projects = projects;
        }
        
        public Map<String, List<String>> getSkillContexts() { return skillContexts; }
        public int getYearsExperience() { return yearsExperience; }
        public List<String> getEducation() { return education; }
        public List<String> getCertifications() { return certifications; }
        public List<String> getProjects() { return projects; }
    }
    
    public static class JobRequirements {
        private Map<String, Integer> skillWeights;
        private Set<String> preferredDegrees;
        private Set<String> preferredCertifications;
        private Set<String> keywords;
        private double experienceWeight;
        private double educationWeight;
        private double certificationWeight;
        private double projectWeight;
        
        // Getters and setters
        public Map<String, Integer> getSkillWeights() { return skillWeights; }
        public void setSkillWeights(Map<String, Integer> skillWeights) { this.skillWeights = skillWeights; }
        
        public Set<String> getPreferredDegrees() { return preferredDegrees; }
        public void setPreferredDegrees(Set<String> preferredDegrees) { this.preferredDegrees = preferredDegrees; }
        
        public Set<String> getPreferredCertifications() { return preferredCertifications; }
        public void setPreferredCertifications(Set<String> preferredCertifications) { this.preferredCertifications = preferredCertifications; }
        
        public Set<String> getKeywords() { return keywords; }
        public void setKeywords(Set<String> keywords) { this.keywords = keywords; }
        
        public double getExperienceWeight() { return experienceWeight; }
        public void setExperienceWeight(double experienceWeight) { this.experienceWeight = experienceWeight; }
        
        public double getEducationWeight() { return educationWeight; }
        public void setEducationWeight(double educationWeight) { this.educationWeight = educationWeight; }
        
        public double getCertificationWeight() { return certificationWeight; }
        public void setCertificationWeight(double certificationWeight) { this.certificationWeight = certificationWeight; }
        
        public double getProjectWeight() { return projectWeight; }
        public void setProjectWeight(double projectWeight) { this.projectWeight = projectWeight; }
    }
    
    // IO methods
    public JobRequirements loadJobRequirements(String filename) throws IOException {
        JobRequirements requirements = new JobRequirements();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        
        Map<String, Integer> skillWeights = new HashMap<>();
        Set<String> degrees = new HashSet<>();
        Set<String> certs = new HashSet<>();
        Set<String> keywords = new HashSet<>();
        
        for (String line : lines) {
            if (line.startsWith("SKILL:")) {
                String[] parts = line.substring(6).split(":");
                skillWeights.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            } else if (line.startsWith("DEGREE:")) {
                degrees.add(line.substring(7).trim());
            } else if (line.startsWith("CERT:")) {
                certs.add(line.substring(5).trim());
            } else if (line.startsWith("KEYWORD:")) {
                keywords.add(line.substring(8).trim());
            }else if (line.contains("WEIGHT_EXPERIENCE")) {
                String value = line.replaceAll(".*?([\\d.]+).*", "$1"); // Extract first number
                requirements.setExperienceWeight(Double.parseDouble(value));
            }
            else if (line.startsWith("WEIGHT_EDUCATION:")) {
                String value = line.replaceAll(".*?([\\d.]+).*", "$1"); // Extract first number
                requirements.setExperienceWeight(Double.parseDouble(value));
            } else if (line.startsWith("WEIGHT_CERTIFICATION:")) {
                 String value = line.replaceAll(".*?([\\d.]+).*", "$1"); // Extract first number
                requirements.setExperienceWeight(Double.parseDouble(value));
            } else if (line.startsWith("WEIGHT_PROJECT:")) {
                 String value = line.replaceAll(".*?([\\d.]+).*", "$1"); // Extract first number
                requirements.setExperienceWeight(Double.parseDouble(value));
            }
        }
        
        requirements.setSkillWeights(skillWeights);
        requirements.setPreferredDegrees(degrees);
        requirements.setPreferredCertifications(certs);
        requirements.setKeywords(keywords);
        
        return requirements;
    }
    
    public List<Resume> loadResumesFromDirectory(String dirPath) throws IOException {
        return Files.walk(Paths.get(dirPath))
                   .filter(Files::isRegularFile)
                   .filter(path -> path.toString().endsWith(".txt") || path.toString().endsWith(".pdf"))
                   .map(path -> {
                       try {
                           String content = path.toString().endsWith(".pdf") 
                               ? extractTextFromPdf(path) 
                               : Files.readString(path);
                           return new Resume(path.getFileName().toString(), content);
                       } catch (IOException e) {
                           System.err.println("Error reading file: " + path);
                           return null;
                       }
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }
    
    private String extractTextFromPdf(Path path) {
        // In a real implementation, you would use a PDF library like Apache PDFBox
        // This is a placeholder for demonstration
        try {
            return "PDF content would be extracted here: " + path.getFileName();
        } catch (Exception e) {
            return "";
        }
    }
    
    public void displayResults(List<RankedResume> rankedResumes) {
        System.out.println("\nTop Candidates:");
        System.out.println("Rank\tName\t\t\tScore\tSummary");
        System.out.println("----------------------------------------");
        
        for (int i = 0; i < Math.min(rankedResumes.size(), 10); i++) {
            RankedResume rr = rankedResumes.get(i);
            String summary = rr.getFeatures().getSkillContexts().keySet().stream()
                             .limit(3)
                             .collect(Collectors.joining(", "));
            
            System.out.printf("%d\t%s\t%.2f\t%s...%n", 
                i+1, 
                rr.getResume().getName(), 
                rr.getScore(),
                summary);
        }
        
        // Option to view detailed analysis
        System.out.println("\nEnter a candidate number to see detailed analysis (0 to exit):");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                int choice = scanner.nextInt();
                if (choice == 0) break;
                if (choice > 0 && choice <= rankedResumes.size()) {
                    RankedResume selected = rankedResumes.get(choice - 1);
                    System.out.println("\nDetailed Analysis for: " + selected.getResume().getName());
                    System.out.println("Score: " + selected.getScore());
                    System.out.println(selected.getAnalysis());
                    System.out.println("\nEnter another number or 0 to exit:");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number");
                scanner.next();
            }
        }
    }
    
    public void saveResultsToFile(List<RankedResume> rankedResumes, String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Rank,Name,Score,Top Skills,Experience,Education");
        
        for (int i = 0; i < rankedResumes.size(); i++) {
            RankedResume rr = rankedResumes.get(i);
            String topSkills = rr.getFeatures().getSkillContexts().keySet().stream()
                               .limit(5)
                               .collect(Collectors.joining(";"));
            String education = String.join(";", rr.getFeatures().getEducation());
            
            lines.add(String.format("%d,%s,%.2f,%s,%d,%s", 
                i+1,
                rr.getResume().getName(),
                rr.getScore(),
                topSkills,
                rr.getFeatures().getYearsExperience(),
                education));
        }
        
        Files.write(Paths.get(filename), lines);
        System.out.println("Results saved to " + filename);
    }
    
    public void setCurrentJobRequirements(JobRequirements requirements) {
        this.currentJobRequirements = requirements;
    }
}