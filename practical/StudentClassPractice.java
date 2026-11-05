
class Student{
    int studentId;
    int age;
    String name;

    Student(){
        this.studentId = 210;
        this.age = 19;
        this.name = "chaitanya";
    }
}

class StudentClassPractice{
    public static void main(String args[]){
        Student s = new Student();
        System.out.println(s.name);
        System.out.println(s.age);
        System.out.println(s.studentId);
    }
}