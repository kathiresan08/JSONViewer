# JSONViewer
An android application to List the contents of a JSON file

1. Whenever an application is launched, it checks for internet connection. **Ref screenshots/1.png**

2. It fetches the json file from provided URL Asynchronously. **Ref screenshots/2.png**

3. It lists the person's thumbnail, name, age fetched from the given json file in custom listview. **Ref screenshots/3.png**

4. In custom listview as you scroll down, the person's thumbnail get downloaded.

5. Stored the thumbnails in hashmap to prevent redownload of thumbnails during recycling of listview.
