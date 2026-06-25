<h3>
 انا كنت ما بدي ارفع التعديلات لانه مش كامل بس يلااااااااااااااااا الله بعيني رفعتهن 
 الي صار انه عملت api جديد ما فيه خيارات الي بتخص الانتتي 
 و عملت هذا ال api  بستقبل ملف هذا الملف هو عباره عن ال ERD  يعني انا لما اروح ارسم ال ERD هي عباره عن ملف dbml 
 هذا الملف هو الي اشتغلت عليه الشغل 
 عملت بارسر بحوله ل EntityRequest -> DTO ثم انشاء عن طريق ال جنريتر للانتتي نفسه 
 لاني غيرت ال dto in new api غيرت كل الفنكشنات الي كانت تستعمله 
 مش غيررررررررتهن ----- كتبت جداد بعتمدن على ال dto الجديد 

 المشاكل : 
 في باااقز بالانتتي ب ال id بتكرر و في مشكله ب المتغير تاع العلاقات 
 بصلحهن بعدين


 test JSON :
 {
  "projectName": "school-system",
  "packageName": "com.example.school",
  "databaseName": "school_db",
  "database": "MYSQL",
  "userFeature": true,
  "securityFeature": true,
  "jwtFeature": true,
  "roleFeature": true,
  "roles": ["ADMIN", "USER"],
  "defaultRole": "USER",
  "swaggerFeature": true
}

tset file:
https://drive.google.com/file/d/1xyzhol7gGiV38l3ob4guY7IxhBsbIOOS/view?usp=sharing

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/1f83bc41-218a-472f-a7d0-fca0d4948d27" />

</h3>





<h3>
 عملت ال validation انت بتحدد شو بدك تحط validation جوا ال entity
</h3>



<h1>Test code:</h1>

<h4>
  {
  "projectName": "school-system",
  "packageName": "com.example.school",
  "database": "MYSQL",
  "databaseName": "school_db",

  "userFeature": true,
  "securityFeature": false,
  "jwtFeature": false,
  "roleFeature": false,
  "swaggerFeature": true,

  "entities": [
    {
      "name": "Student",
      "fields": [
        {
          "name": "firstName",
          "type": "String"
        },
        {
          "name": "age",
          "type": "Integer"
        },
        {
            "name": "date",
            "type": "LocalDateTime"
        }
      ],
      "relations": [
        {
          "type": "MANY_TO_ONE",
          "targetEntity": "ClassRoom",
          "fieldName": "classRoom"
        }
      ]
    },
    {
      "name": "ClassRoom",
      "fields": [
        {
          "name": "name",
          "type": "String"
        }
      ],
      "relations": [
  {
    "type": "ONE_TO_MANY",
    "targetEntity": "Student",
    "fieldName": "students",
    "mappedBy": "classRoom"
  }
]
    }
  ]
}
</h4>


<h1> Test code with validation </h1>

<h4> 

{
  "projectName": "school-system",
  "packageName": "com.example.school",
  "database": "MYSQL",
  "databaseName": "school_db",
  "userFeature": true,
  "securityFeature": false,
  "jwtFeature": false,
  "roleFeature": false,
  "swaggerFeature": true,
  "entities": [
    {
      "name": "Student",
      "fields": [
        {
          "name": "firstName",
          "type": "String",
          "validations": [
            {
              "type": "NOT_BLANK",
              "message": "First name is required"
            },
            {
              "type": "SIZE",
              "min": 2,
              "max": 50,
              "message": "First name must be between 2 and 50 characters"
            }
          ]
        },
        {
          "name": "age",
          "type": "Integer",
          "validations": [
            {
              "type": "NOT_NULL",
              "message": "Age is required"
            },
            {
              "type": "MIN",
              "min": 6,
              "message": "Age must be at least 6"
            },
            {
              "type": "MAX",
              "max": 100,
              "message": "Age must be less than 100"
            }
          ]
        },
        {
          "name": "email",
          "type": "String",
          "validations": [
            {
              "type": "NOT_BLANK",
              "message": "Email is required"
            },
            {
              "type": "EMAIL",
              "message": "Invalid email format"
            }
          ]
        },
        {
          "name": "date",
          "type": "LocalDateTime"
        }
      ],
      "relations": [
        {
          "type": "MANY_TO_ONE",
          "targetEntity": "ClassRoom",
          "fieldName": "classRoom"
        }
      ]
    },
    {
      "name": "ClassRoom",
      "fields": [
        {
          "name": "name",
          "type": "String",
          "validations": [
            {
              "type": "NOT_BLANK",
              "message": "Classroom name is required"
            },
            {
              "type": "SIZE",
              "min": 2,
              "max": 30,
              "message": "Classroom name must be between 2 and 30 characters"
            }
          ]
        }
      ],
      "relations": [
        {
          "type": "ONE_TO_MANY",
          "targetEntity": "Student",
          "fieldName": "students",
          "mappedBy": "classRoom"
        }
      ]
    }
  ]
}

</h4>
