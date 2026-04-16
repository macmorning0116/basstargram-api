-- 소셜 프로필 사진 URL을 null로 초기화 (커스텀 업로드만 유지)
UPDATE users
SET profile_image_url = NULL
WHERE profile_image_url IS NOT NULL
  AND profile_image_url NOT LIKE '/uploads/%'
  AND profile_image_url NOT LIKE '%s3.ap-northeast-2.amazonaws.com%';
