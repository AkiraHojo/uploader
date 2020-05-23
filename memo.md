
```java
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

```

```java
    public ResponseEntity<String> serveFile(@PathVariable String filename) {
```

```java
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, 
                    "text/html; charset=UTF-8").body(file);
```

```java
        String safeOutput = "";
        try {
            BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result;
                result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            // safeOutput = ESAPI.encoder().encodeForHTML(buf.toString("UTF-8"));
            safeOutput = buf.toString("UTF-8").replaceAll("&", "&amp").replaceAll("<", "&lt").replaceAll(">", "&gt");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, 
        "text/html; charset=UTF-8").body(safeOutput);

```

