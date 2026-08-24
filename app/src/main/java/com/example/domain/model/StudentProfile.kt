package com.example.domain.model

data class BoardInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val state: String,
    val country: String = "India",
    val isNational: Boolean = false,
    val supportedClasses: List<String> = defaultClasses(),
    val curriculumMetadata: String = "Curriculum aligned with national and state educational framework"
)

data class StudentProfile(
    val boardId: String = "jkbose",
    val boardName: String = "JKBOSE",
    val state: String = "Jammu & Kashmir",
    val classLevel: String = "Class 10",
    val subject: String = "Science",
    val language: String = "English"
)

fun defaultClasses(): List<String> = listOf(
    "Class 5",
    "Class 6",
    "Class 7",
    "Class 8",
    "Class 9",
    "Class 10",
    "Class 11",
    "Class 12",
    "Graduation / Undergraduate"
)

fun defaultStates(): List<String> = listOf(
    "Jammu & Kashmir",
    "National (All India)",
    "Maharashtra",
    "Delhi",
    "Uttar Pradesh",
    "Rajasthan",
    "Punjab",
    "Haryana",
    "Tamil Nadu",
    "Karnataka",
    "Gujarat",
    "West Bengal",
    "Bihar",
    "Kerala",
    "Madhya Pradesh",
    "Andhra Pradesh",
    "Telangana",
    "Odisha",
    "Assam",
    "Uttarakhand",
    "Himachal Pradesh",
    "Jharkhand",
    "Chhattisgarh",
    "Goa"
)

fun defaultLanguages(): List<String> = listOf(
    "English",
    "Hinglish / Hindi",
    "Urdu",
    "Regional / Native"
)

fun defaultSubjectsForClass(classLevel: String): List<String> {
    return when {
        classLevel.contains("11") || classLevel.contains("12") -> listOf(
            "Physics",
            "Chemistry",
            "Mathematics",
            "Biology",
            "Computer Science",
            "English",
            "Economics",
            "Accountancy",
            "Business Studies",
            "History",
            "Political Science",
            "Urdu",
            "Hindi"
        )
        classLevel.contains("Graduation") -> listOf(
            "Computer Science & Engineering",
            "Data Structures & Algorithms",
            "Physics",
            "Organic Chemistry",
            "Calculus & Higher Math",
            "Economics & Finance",
            "Electrical Engineering",
            "Mechanical Engineering",
            "Biotechnology",
            "Commerce & Accounting",
            "General Science"
        )
        else -> listOf(
            "Science",
            "Mathematics",
            "Social Science",
            "English",
            "Hindi",
            "Urdu",
            "History",
            "Geography",
            "General Knowledge",
            "Computer Basics"
        )
    }
}

fun allSupportedBoards(): List<BoardInfo> = listOf(
    BoardInfo(
        id = "jkbose",
        name = "JKBOSE",
        fullName = "Jammu & Kashmir State Board of School Education",
        state = "Jammu & Kashmir",
        isNational = false,
        curriculumMetadata = "JKBOSE prescribed syllabus with special focus on state board pattern & exam style"
    ),
    BoardInfo(
        id = "cbse",
        name = "CBSE",
        fullName = "Central Board of Secondary Education",
        state = "National (All India)",
        isNational = true,
        curriculumMetadata = "NCERT / CBSE standard syllabus with competency-based questions and active recall"
    ),
    BoardInfo(
        id = "cisce",
        name = "CISCE / ICSE",
        fullName = "Council for the Indian School Certificate Examinations",
        state = "National (All India)",
        isNational = true,
        curriculumMetadata = "ICSE/ISC deep analytical syllabus with comprehensive problem-solving"
    ),
    BoardInfo(
        id = "nios",
        name = "NIOS",
        fullName = "National Institute of Open Schooling",
        state = "National (All India)",
        isNational = true,
        curriculumMetadata = "NIOS self-paced open curriculum"
    ),
    BoardInfo(
        id = "msbshse",
        name = "Maharashtra State Board",
        fullName = "Maharashtra State Board of Secondary and Higher Secondary Education",
        state = "Maharashtra",
        isNational = false,
        curriculumMetadata = "Balbharati state curriculum with board examination pattern"
    ),
    BoardInfo(
        id = "upmsp",
        name = "UP Board",
        fullName = "Uttar Pradesh Madhyamik Shiksha Parishad",
        state = "Uttar Pradesh",
        isNational = false,
        curriculumMetadata = "UP Board NCERT-pattern syllabus and board question banks"
    ),
    BoardInfo(
        id = "rbse",
        name = "Rajasthan Board (RBSE)",
        fullName = "Rajasthan Board of Secondary Education",
        state = "Rajasthan",
        isNational = false,
        curriculumMetadata = "RBSE curriculum with state-level model papers"
    ),
    BoardInfo(
        id = "pseb",
        name = "Punjab Board (PSEB)",
        fullName = "Punjab School Education Board",
        state = "Punjab",
        isNational = false,
        curriculumMetadata = "PSEB syllabus and annual board examination pattern"
    ),
    BoardInfo(
        id = "bseh",
        name = "Haryana Board (BSEH)",
        fullName = "Board of School Education Haryana",
        state = "Haryana",
        isNational = false,
        curriculumMetadata = "BSEH semester & annual examination pattern"
    ),
    BoardInfo(
        id = "tndge",
        name = "Tamil Nadu State Board",
        fullName = "Tamil Nadu Directorate of Government Examinations (Samacheer Kalvi)",
        state = "Tamil Nadu",
        isNational = false,
        curriculumMetadata = "Samacheer Kalvi state curriculum"
    ),
    BoardInfo(
        id = "kseab",
        name = "Karnataka Board (KSEAB)",
        fullName = "Karnataka School Examination and Assessment Board",
        state = "Karnataka",
        isNational = false,
        curriculumMetadata = "KSEAB state board syllabus"
    ),
    BoardInfo(
        id = "gseb",
        name = "Gujarat Board (GSEB)",
        fullName = "Gujarat Secondary and Higher Secondary Education Board",
        state = "Gujarat",
        isNational = false,
        curriculumMetadata = "GSEB curriculum and textbook solutions"
    ),
    BoardInfo(
        id = "wbbse",
        name = "West Bengal Board",
        fullName = "West Bengal Board of Secondary Education (WBBSE / WBCHSE)",
        state = "West Bengal",
        isNational = false,
        curriculumMetadata = "West Bengal Madhyamik and Higher Secondary curriculum"
    ),
    BoardInfo(
        id = "bseb",
        name = "Bihar Board (BSEB)",
        fullName = "Bihar School Examination Board",
        state = "Bihar",
        isNational = false,
        curriculumMetadata = "BSEB state syllabus and objective question model"
    ),
    BoardInfo(
        id = "kerala_dhse",
        name = "Kerala State Board",
        fullName = "Kerala Directorate of Higher Secondary Education / SCERT",
        state = "Kerala",
        isNational = false,
        curriculumMetadata = "Kerala SCERT curriculum"
    )
)
