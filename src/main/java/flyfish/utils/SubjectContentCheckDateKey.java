package flyfish.utils;

import java.time.LocalDate;
import java.util.Objects;

public class SubjectContentCheckDateKey {
    private final String subject;  
    private final String content;  
    private final LocalDate checkDate;
  
    public SubjectContentCheckDateKey(String subject, String content, LocalDate checkDate) {  
        this.subject = subject;  
        this.content = content;  
        this.checkDate = checkDate;  
    }  
  
    // getters  
    public String getSubject() {  
        return subject;  
    }  
  
    public String getContent() {  
        return content;  
    }  
  
    public LocalDate getCheckDate() {  
        return checkDate;  
    }  
  
    // hashCode and equals methods are required for use as a Map key  
    @Override  
    public int hashCode() {  
        // Implement a proper hash code calculation using all fields  
        // For simplicity, using Objects.hash() from Java 7+  
        return Objects.hash(subject, content, checkDate);
    }  
  
    @Override  
    public boolean equals(Object obj) {  
        if (this == obj) return true;  
        if (obj == null || getClass() != obj.getClass()) return false;  
        SubjectContentCheckDateKey other = (SubjectContentCheckDateKey) obj;  
        return Objects.equals(subject, other.subject) &&  
               Objects.equals(content, other.content) &&  
               Objects.equals(checkDate, other.checkDate);  
    }  
  
    // toString method is optional but useful for debugging  
    @Override  
    public String toString() {  
        return "SubjectContentCheckDateKey{" +  
                "subject='" + subject + '\'' +  
                ", content='" + content + '\'' +  
                ", checkDate=" + checkDate +  
                '}';  
    }  
}