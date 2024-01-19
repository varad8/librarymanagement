# Library Management System Android App

The Library Management System Android app is a powerful solution for managing library operations seamlessly. This app is built using Android (Java/Kotlin) for the frontend and Firebase (Realtime Database, Authentication, Cloud Storage) for the backend, providing real-time data synchronization and cloud storage for a reliable and scalable library management experience.

## Demo

Check out the live demo of the project [here](https://www.vrnitsolution.tech/projects/f77d577f-55fe-4683-8882-8777b0d34e92).

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features

### General Features

1. **Add New Books:**

   - Admins can add new books to the library by providing details such as title, ISBN number, quantity, category, and rack number.

2. **Update Existing Books:**

   - Admins can update book details, including title, ISBN number, quantity, category, and rack number.

3. **Issue Books to Users:**

   - Admins can issue books to users by entering the library ID of the student. The system searches for the user in records and displays the user's profile.

4. **Update Issued Books Status:**

   - Admins can update the status of issued books, keeping track of their circulation in the library.

5. **Collect Fine for Overdue Books:**
   - A fine of ₹10 per day is automatically calculated for overdue books. Admins can collect fines when users return books.

### Admin-Specific Features

1. **Profile Completion Pop-up:**

   - Admins are prompted to complete their profile for a comprehensive dashboard experience when logging in for the first time.

2. **Adding a New Book:**

   - Admins can add new books to the library by navigating to the "Add New Book" section in the admin dashboard.

3. **Updating a Book:**

   - Admins can update book details by choosing the "Update" action in the "All Books" section and providing necessary details.

4. **Deleting a Book:**

   - Admins can delete books from the library by selecting the book in the "All Books" section and choosing the "Delete" action.

5. **Issuing and Returning Books:**

   - Admins can issue books to users and handle book returns efficiently through the admin dashboard.

6. **Viewing Issued and Returned Book Data:**
   - Admins have access to detailed information on issued and returned books, allowing them to track library activity.

### User-Specific Features

1. **Profile Completion Pop-up:**

   - Users are encouraged to complete their profiles for a comprehensive dashboard experience upon the first login.

2. **My Issued Books:**

   - Users can view and track all books they currently have issued in the "My Issued Books" section.

3. **My Returned Books:**

   - The "My Returned Books" section displays a comprehensive list of books that the user has previously returned.

4. **All Books:**

   - Users can explore the entire library catalog in the "All Books" section, providing a centralized view of the available books.

5. **Updating Profile Details:**
   - Users can update their profile details at any time, ensuring accurate and up-to-date information.

## Technology Stack

- **Frontend:** Android (Java/Kotlin)
- **Backend:** Firebase (Realtime Database, Authentication, Cloud Storage)

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/library-management-android-app.git
   cd library-management-android-app
   ```

2. Open the project in Android Studio and build/run the app.

## Usage

1. Login using your admin or user credentials.
2. Navigate through the dashboard to manage books, issue/return books, and view activity.

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the [MIT License](LICENSE).
