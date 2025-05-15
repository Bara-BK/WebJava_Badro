# Database Update Instructions

The application has been updated to include a new "destination" field for experiences. You must run the included SQL script to update your database schema.

## Running the Update

1. Make sure your database server is running
2. Execute the SQL script `update_experience_table.sql` on your database
3. You can run this script using:
   - Your database management tool (e.g., MySQL Workbench, pgAdmin)
   - Command line client for your database

## What This Update Does

The update script will:
1. Add a new `destination` column to the `experience` table
2. Set a default empty value for existing records

## Troubleshooting

If you encounter any issues:
1. Check that your database server is running
2. Ensure you have proper permissions to alter the table
3. Verify the `experience` table exists in your database

If problems persist, contact the development team for assistance. 