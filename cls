[1mdiff --git a/src/main/java/com/assetcontrol/imports/web/AssignmentImportController.java b/src/main/java/com/assetcontrol/imports/web/AssignmentImportController.java[m
[1mindex b9405ce..c82425f 100644[m
[1m--- a/src/main/java/com/assetcontrol/imports/web/AssignmentImportController.java[m
[1m+++ b/src/main/java/com/assetcontrol/imports/web/AssignmentImportController.java[m
[36m@@ -54,8 +54,12 @@[m [mpublic class AssignmentImportController {[m
     }[m
 [m
     @PostMapping("/phones/preview")[m
[31m-    public String previewPhones(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {[m
[31m-        return previewPhones(file, session, model);[m
[32m+[m[32m    public String previewPhones([m
[32m+[m[32m            @RequestParam("file") MultipartFile file,[m
[32m+[m[32m            HttpSession session,[m
[32m+[m[32m            Model model[m
[32m+[m[32m    ) {[m
[32m+[m[32m        return processPhonePreview(file, session, model);[m
     }[m
 [m
     @PostMapping("/computers/confirm")[m
[36m@@ -84,7 +88,7 @@[m [mpublic class AssignmentImportController {[m
         return "redirect:/phones";[m
     }[m
 [m
[31m-    private String previewPhones(MultipartFile file, HttpSession session, Model model) {[m
[32m+[m[32m    private String processPhonePreview(MultipartFile file, HttpSession session, Model model) {[m
         try {[m
             AssignmentImportPreview preview = phoneImportService.previewPhones(file);[m
             session.setAttribute(PHONE_PREVIEW_KEY, preview);[m
