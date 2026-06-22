<h3> عملت المستخدم بقدر يعمل انتتي قد ما بده و بالمتغيرات الي بده اياها و العلاقات نصهن تم ,ظل النص الثاني لبكرا و بسسسسسسسسسسس</h3>
<h2>عملت البق تاع ال enum كمان</h2>


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
