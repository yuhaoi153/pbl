-- 在云服务器的 MySQL 上执行一次。
-- 保留原列中的所有数据，只将列名从 senderName 改为 superVisor。
ALTER TABLE miniprograme.hardwareMessage
    CHANGE COLUMN senderName superVisor VARCHAR(100) NOT NULL;

-- 修正旧版本按键回复中误存的设备所属学生姓名。
UPDATE miniprograme.hardwareMessage AS message
JOIN miniprograme.hardwareDevice AS device
  ON message.deviceName = CONCAT(device.deviceType, device.deviceNum)
SET message.superVisor = device.superVisor
WHERE message.direction = 'toSoftware'
  AND message.messageType = 'buttonReply'
  AND device.superVisor IS NOT NULL
  AND device.superVisor <> '';

-- 执行后可用下面语句确认：
SHOW COLUMNS FROM miniprograme.hardwareMessage LIKE 'superVisor';
