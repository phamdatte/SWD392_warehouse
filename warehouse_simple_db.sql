-- =====================================================
-- WAREHOUSE MANAGEMENT SYSTEM
-- FINAL OPTIMIZED DATABASE SCRIPT
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS inventory_transaction;
DROP TABLE IF EXISTS goods_issue_item;
DROP TABLE IF EXISTS goods_issue;
DROP TABLE IF EXISTS goods_receipt_item;
DROP TABLE IF EXISTS goods_receipt;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS vendor;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS role_page;
DROP TABLE IF EXISTS page;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS config;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- CONFIG
-- Dùng config_id INT thay config_key làm PK
-- → JPA dễ map @Entity hơn
-- =====================================================

CREATE TABLE config (
                        config_id    INT AUTO_INCREMENT PRIMARY KEY,
                        config_key   VARCHAR(50) NOT NULL UNIQUE,
                        config_value TEXT,
                        description  VARCHAR(255),
                        updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- ROLE
-- Bỏ created_at — bảng lookup cố định, không cần track
-- =====================================================

CREATE TABLE role (
                      role_id     INT AUTO_INCREMENT PRIMARY KEY,
                      role_name   VARCHAR(50) NOT NULL UNIQUE,
                      description VARCHAR(255),
                      is_active   BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- PAGE
-- Bỏ created_at — config hệ thống, không cần track
-- icon tăng lên VARCHAR(100) cho đủ class dài
-- =====================================================

CREATE TABLE page (
                      page_id       INT AUTO_INCREMENT PRIMARY KEY,
                      page_code     VARCHAR(50) NOT NULL UNIQUE,
                      page_name     VARCHAR(100) NOT NULL,
                      page_url      VARCHAR(200) NOT NULL,
                      page_group    VARCHAR(50),
                      icon          VARCHAR(100),
                      display_order INT DEFAULT 0,
                      is_menu       BOOLEAN DEFAULT TRUE,
                      is_active     BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- ROLE_PAGE
-- =====================================================

CREATE TABLE role_page (
                           role_id     INT NOT NULL,
                           page_id     INT NOT NULL,
                           can_view    BOOLEAN DEFAULT TRUE,
                           can_create  BOOLEAN DEFAULT FALSE,
                           can_edit    BOOLEAN DEFAULT FALSE,
                           can_delete  BOOLEAN DEFAULT FALSE,
                           can_approve BOOLEAN DEFAULT FALSE,
                           PRIMARY KEY (role_id, page_id),
                           FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
                           FOREIGN KEY (page_id) REFERENCES page(page_id) ON DELETE CASCADE
);

-- =====================================================
-- USER
-- =====================================================

CREATE TABLE user (
                      user_id       INT AUTO_INCREMENT PRIMARY KEY,
                      username      VARCHAR(50) NOT NULL UNIQUE,
                      password_hash VARCHAR(255) NOT NULL,
                      full_name     VARCHAR(100) NOT NULL,
                      role_id       INT NOT NULL,
                      email         VARCHAR(100) UNIQUE,
                      phone         VARCHAR(20),
                      is_active     BOOLEAN DEFAULT TRUE,
                      created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,
                      FOREIGN KEY (role_id) REFERENCES role(role_id)
);

CREATE INDEX idx_user_role ON user(role_id);

-- =====================================================
-- VENDOR
-- Thêm updated_at để track khi sửa thông tin NCC
-- =====================================================

CREATE TABLE vendor (
                        vendor_id      INT AUTO_INCREMENT PRIMARY KEY,
                        vendor_code    VARCHAR(20) NOT NULL UNIQUE,
                        vendor_name    VARCHAR(100) NOT NULL,
                        contact_person VARCHAR(100),
                        phone          VARCHAR(20),
                        email          VARCHAR(100),
                        address        TEXT,
                        is_active      BOOLEAN DEFAULT TRUE,
                        created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- CUSTOMER
-- Thêm updated_at để track khi sửa thông tin KH
-- =====================================================

CREATE TABLE customer (
                          customer_id    INT AUTO_INCREMENT PRIMARY KEY,
                          customer_code  VARCHAR(20) NOT NULL UNIQUE,
                          customer_name  VARCHAR(100) NOT NULL,
                          contact_person VARCHAR(100),
                          phone          VARCHAR(20),
                          email          VARCHAR(100),
                          address        TEXT,
                          is_active      BOOLEAN DEFAULT TRUE,
                          created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- PRODUCT CATEGORY
-- Bảng lookup đơn giản, không cần timestamp
-- =====================================================

CREATE TABLE product_category (
                                  category_id   INT AUTO_INCREMENT PRIMARY KEY,
                                  category_name VARCHAR(50) NOT NULL UNIQUE,
                                  description   VARCHAR(255),
                                  is_active     BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- PRODUCT
-- =====================================================

CREATE TABLE product (
                         product_id   INT AUTO_INCREMENT PRIMARY KEY,
                         product_code VARCHAR(20) NOT NULL UNIQUE,
                         barcode      VARCHAR(50) UNIQUE,
                         product_name VARCHAR(100) NOT NULL,
                         description  TEXT,
                         unit         VARCHAR(20) NOT NULL,
                         unit_price   DECIMAL(15,2) DEFAULT 0,
                         category_id  INT,
                         is_active    BOOLEAN DEFAULT TRUE,
                         created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (category_id)
                             REFERENCES product_category(category_id)
);

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_barcode  ON product(barcode);

-- =====================================================
-- INVENTORY
-- product_id là PK — mỗi sản phẩm chỉ có 1 dòng tồn kho
-- CHECK quantity >= 0 tránh âm kho ở tầng DB
-- =====================================================

CREATE TABLE inventory (
                           product_id   INT PRIMARY KEY,
                           quantity     DECIMAL(12,2) DEFAULT 0 CHECK (quantity >= 0),
                           last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- =====================================================
-- GOODS RECEIPT (Phiếu nhập kho)
-- total_amount bỏ — tính động từ SUM(subtotal) trong view
-- =====================================================

CREATE TABLE goods_receipt (
                               receipt_id     INT AUTO_INCREMENT PRIMARY KEY,
                               receipt_number VARCHAR(20) NOT NULL UNIQUE,
                               vendor_id      INT NOT NULL,
                               receipt_date   DATETIME NOT NULL,
                               status         ENUM('Pending','Approved','Completed','Cancelled')
                   NOT NULL DEFAULT 'Pending',
                               notes          TEXT,
                               created_by     INT NOT NULL,
                               approved_by    INT,
                               approved_at    DATETIME,
                               created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                               updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
                               FOREIGN KEY (vendor_id)   REFERENCES vendor(vendor_id),
                               FOREIGN KEY (created_by)  REFERENCES user(user_id),
                               FOREIGN KEY (approved_by) REFERENCES user(user_id)
);

CREATE INDEX idx_receipt_vendor ON goods_receipt(vendor_id);
CREATE INDEX idx_receipt_status ON goods_receipt(status);
CREATE INDEX idx_receipt_date   ON goods_receipt(receipt_date);

CREATE TABLE goods_receipt_item (
                                    receipt_item_id INT AUTO_INCREMENT PRIMARY KEY,
                                    receipt_id      INT NOT NULL,
                                    product_id      INT NOT NULL,
                                    quantity        DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
                                    unit_price      DECIMAL(15,2) NOT NULL CHECK (unit_price >= 0),
                                    subtotal        DECIMAL(15,2)
                                        GENERATED ALWAYS AS (quantity * unit_price) STORED,
                                    UNIQUE (receipt_id, product_id),
                                    FOREIGN KEY (receipt_id)
                                        REFERENCES goods_receipt(receipt_id) ON DELETE CASCADE,
                                    FOREIGN KEY (product_id)
                                        REFERENCES product(product_id)
);

CREATE INDEX idx_receipt_item_product ON goods_receipt_item(product_id);

-- =====================================================
-- GOODS ISSUE (Phiếu xuất kho)
-- customer_id nullable — cho phép xuất không cần KH cụ thể
-- =====================================================

CREATE TABLE goods_issue (
                             issue_id     INT AUTO_INCREMENT PRIMARY KEY,
                             issue_number VARCHAR(20) NOT NULL UNIQUE,
                             customer_id  INT,
                             issue_date   DATETIME NOT NULL,
                             status       ENUM('Pending','Approved','Completed','Cancelled')
                 NOT NULL DEFAULT 'Pending',
                             notes        TEXT,
                             created_by   INT NOT NULL,
                             approved_by  INT,
                             approved_at  DATETIME,
                             created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (customer_id)  REFERENCES customer(customer_id),
                             FOREIGN KEY (created_by)   REFERENCES user(user_id),
                             FOREIGN KEY (approved_by)  REFERENCES user(user_id)
);

CREATE INDEX idx_issue_customer ON goods_issue(customer_id);
CREATE INDEX idx_issue_status   ON goods_issue(status);
CREATE INDEX idx_issue_date     ON goods_issue(issue_date);

CREATE TABLE goods_issue_item (
                                  issue_item_id INT AUTO_INCREMENT PRIMARY KEY,
                                  issue_id      INT NOT NULL,
                                  product_id    INT NOT NULL,
                                  quantity      DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
                                  unit_price    DECIMAL(15,2) NOT NULL CHECK (unit_price >= 0),
                                  subtotal      DECIMAL(15,2)
                                      GENERATED ALWAYS AS (quantity * unit_price) STORED,
                                  UNIQUE (issue_id, product_id),
                                  FOREIGN KEY (issue_id)
                                      REFERENCES goods_issue(issue_id) ON DELETE CASCADE,
                                  FOREIGN KEY (product_id)
                                      REFERENCES product(product_id)
);

CREATE INDEX idx_issue_item_product ON goods_issue_item(product_id);

-- =====================================================
-- INVENTORY TRANSACTION (Lịch sử giao dịch)
-- Giữ lại cột notes để ghi chú khi cần điều chỉnh thủ công
-- =====================================================

CREATE TABLE inventory_transaction (
                                       transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
                                       product_id       INT NOT NULL,
                                       transaction_type ENUM('Receipt','Issue') NOT NULL,
                                       quantity         DECIMAL(12,2) NOT NULL,
                                       quantity_before  DECIMAL(12,2) NOT NULL,
                                       quantity_after   DECIMAL(12,2) NOT NULL,
                                       reference_id     INT,
                                       reference_type   ENUM('GoodsReceipt','GoodsIssue'),
                                       notes            TEXT,
                                       performed_by     INT NOT NULL,
                                       transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (product_id)   REFERENCES product(product_id),
                                       FOREIGN KEY (performed_by) REFERENCES user(user_id)
);

CREATE INDEX idx_transaction_product
    ON inventory_transaction(product_id);
CREATE INDEX idx_transaction_date
    ON inventory_transaction(transaction_date);
CREATE INDEX idx_transaction_reference
    ON inventory_transaction(reference_id, reference_type);

-- =====================================================
-- VIEWS
-- =====================================================

-- Tồn kho hiện tại
CREATE VIEW v_inventory_status AS
SELECT
    p.product_id,
    p.product_code,
    p.barcode,
    p.product_name,
    c.category_name,
    p.unit,
    p.unit_price,
    COALESCE(i.quantity, 0)                AS current_quantity,
    COALESCE(i.quantity, 0) * p.unit_price AS inventory_value
FROM product p
         LEFT JOIN product_category c ON p.category_id = c.category_id
         LEFT JOIN inventory i         ON p.product_id  = i.product_id
WHERE p.is_active = TRUE;

-- Menu theo user (dùng cho frontend hiển thị sidebar)
CREATE VIEW v_user_menu AS
SELECT
    u.user_id,
    u.username,
    r.role_name,
    p.page_id,
    p.page_code,
    p.page_name,
    p.page_url,
    p.page_group,
    p.icon,
    p.display_order,
    p.is_menu,
    rp.can_view,
    rp.can_create,
    rp.can_edit,
    rp.can_delete,
    rp.can_approve
FROM user u
         JOIN role      r  ON u.role_id  = r.role_id
         JOIN role_page rp ON r.role_id  = rp.role_id
         JOIN page      p  ON rp.page_id = p.page_id
WHERE u.is_active  = TRUE
  AND r.is_active  = TRUE
  AND p.is_active  = TRUE
  AND rp.can_view  = TRUE
ORDER BY p.display_order;

-- Tóm tắt phiếu nhập (total_amount tính động)
CREATE VIEW v_receipt_summary AS
SELECT
    gr.receipt_id,
    gr.receipt_number,
    gr.receipt_date,
    v.vendor_name,
    gr.status,
    COALESCE(SUM(gri.subtotal), 0) AS total_amount,
    u1.full_name                   AS created_by_name,
    u2.full_name                   AS approved_by_name,
    COUNT(gri.receipt_item_id)     AS item_count
FROM goods_receipt gr
         JOIN vendor v ON gr.vendor_id = v.vendor_id
         JOIN user  u1 ON gr.created_by  = u1.user_id
         LEFT JOIN user u2 ON gr.approved_by = u2.user_id
         LEFT JOIN goods_receipt_item gri ON gr.receipt_id = gri.receipt_id
GROUP BY gr.receipt_id, gr.receipt_number, gr.receipt_date,
         v.vendor_name, gr.status, u1.full_name, u2.full_name;

-- Tóm tắt phiếu xuất
CREATE VIEW v_issue_summary AS
SELECT
    gi.issue_id,
    gi.issue_number,
    gi.issue_date,
    c.customer_name,
    gi.status,
    COALESCE(SUM(gii.subtotal), 0) AS total_amount,
    u1.full_name                   AS created_by_name,
    u2.full_name                   AS approved_by_name,
    COUNT(gii.issue_item_id)       AS item_count
FROM goods_issue gi
         LEFT JOIN customer c ON gi.customer_id = c.customer_id
         JOIN  user u1 ON gi.created_by  = u1.user_id
         LEFT JOIN user u2 ON gi.approved_by = u2.user_id
         LEFT JOIN goods_issue_item gii ON gi.issue_id = gii.issue_id
GROUP BY gi.issue_id, gi.issue_number, gi.issue_date,
         c.customer_name, gi.status, u1.full_name, u2.full_name;

-- =====================================================
-- DEFAULT DATA
-- =====================================================

INSERT INTO config (config_key, config_value, description) VALUES
                                                               ('system_name', 'Warehouse Management System', 'Tên hệ thống'),
                                                               ('currency',    'VND',                          'Đơn vị tiền tệ');

INSERT INTO role (role_name, description) VALUES
                                              ('Admin',   'Quản trị viên - Toàn quyền'),
                                              ('Manager', 'Quản lý kho - Duyệt phiếu, xem báo cáo'),
                                              ('Staff',   'Nhân viên kho - Tạo phiếu nhập/xuất');

INSERT INTO page (page_code, page_name, page_url, page_group, icon, display_order, is_menu) VALUES
                                                                                                ('dashboard',         'Dashboard',             '/dashboard',             'dashboard', 'fa-home',         1,  TRUE),
                                                                                                ('receipt_list',      'Danh sách phiếu nhập',  '/receipt',               'receipt',   'fa-inbox',        10, TRUE),
                                                                                                ('receipt_create',    'Tạo phiếu nhập',        '/receipt/create',        'receipt',   'fa-plus',         11, TRUE),
                                                                                                ('receipt_detail',    'Chi tiết phiếu nhập',   '/receipt/:id',           'receipt',   NULL,              12, FALSE),
                                                                                                ('receipt_approve',   'Duyệt phiếu nhập',      '/receipt/approve',       'receipt',   'fa-check',        13, TRUE),
                                                                                                ('issue_list',        'Danh sách phiếu xuất',  '/issue',                 'issue',     'fa-share',        20, TRUE),
                                                                                                ('issue_create',      'Tạo phiếu xuất',        '/issue/create',          'issue',     'fa-plus',         21, TRUE),
                                                                                                ('issue_detail',      'Chi tiết phiếu xuất',   '/issue/:id',             'issue',     NULL,              22, FALSE),
                                                                                                ('issue_approve',     'Duyệt phiếu xuất',      '/issue/approve',         'issue',     'fa-check',        23, TRUE),
                                                                                                ('inventory_list',    'Tồn kho',               '/inventory',             'inventory', 'fa-boxes',        30, TRUE),
                                                                                                ('inventory_history', 'Lịch sử giao dịch',     '/inventory/history',     'inventory', 'fa-history',      31, TRUE),
                                                                                                ('report_receipt',    'Báo cáo nhập kho',      '/report/receipt',        'report',    'fa-chart-bar',    40, TRUE),
                                                                                                ('report_issue',      'Báo cáo xuất kho',      '/report/issue',          'report',    'fa-chart-line',   41, TRUE),
                                                                                                ('report_inventory',  'Báo cáo tồn kho',       '/report/inventory',      'report',    'fa-chart-pie',    42, TRUE),
                                                                                                ('product_list',      'Quản lý sản phẩm',      '/product',               'master',    'fa-box',          50, TRUE),
                                                                                                ('category_list',     'Quản lý danh mục',      '/category',              'master',    'fa-tags',         51, TRUE),
                                                                                                ('vendor_list',       'Quản lý nhà cung cấp',  '/vendor',                'master',    'fa-truck',        52, TRUE),
                                                                                                ('customer_list',     'Quản lý khách hàng',    '/customer',              'master',    'fa-user',         53, TRUE),
                                                                                                ('user_list',         'Quản lý người dùng',    '/admin/user',            'admin',     'fa-users',        60, TRUE),
                                                                                                ('role_list',         'Quản lý vai trò',       '/admin/role',            'admin',     'fa-user-shield',  61, TRUE),
                                                                                                ('page_permission',   'Phân quyền trang',      '/admin/page-permission', 'admin',     'fa-lock',         62, TRUE);

-- Admin: full access
INSERT INTO role_page (role_id, page_id, can_view, can_create, can_edit, can_delete, can_approve)
SELECT 1, page_id, TRUE, TRUE, TRUE, TRUE, TRUE FROM page;

-- Manager: duyệt phiếu + xem báo cáo + xem tồn kho
INSERT INTO role_page (role_id, page_id, can_view, can_create, can_edit, can_delete, can_approve) VALUES
                                                                                                      (2, 1,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- dashboard
                                                                                                      (2, 2,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- receipt_list
                                                                                                      (2, 4,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- receipt_detail
                                                                                                      (2, 5,  TRUE, FALSE, FALSE, FALSE, TRUE),   -- receipt_approve
                                                                                                      (2, 6,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- issue_list
                                                                                                      (2, 8,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- issue_detail
                                                                                                      (2, 9,  TRUE, FALSE, FALSE, FALSE, TRUE),   -- issue_approve
                                                                                                      (2, 10, TRUE, FALSE, FALSE, FALSE, FALSE),  -- inventory_list
                                                                                                      (2, 11, TRUE, FALSE, FALSE, FALSE, FALSE),  -- inventory_history
                                                                                                      (2, 12, TRUE, FALSE, FALSE, FALSE, FALSE),  -- report_receipt
                                                                                                      (2, 13, TRUE, FALSE, FALSE, FALSE, FALSE),  -- report_issue
                                                                                                      (2, 14, TRUE, FALSE, FALSE, FALSE, FALSE);  -- report_inventory

-- Staff: tạo phiếu nhập/xuất + xem tồn kho
INSERT INTO role_page (role_id, page_id, can_view, can_create, can_edit, can_delete, can_approve) VALUES
                                                                                                      (3, 1,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- dashboard
                                                                                                      (3, 2,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- receipt_list
                                                                                                      (3, 3,  TRUE, TRUE,  TRUE,  FALSE, FALSE),  -- receipt_create
                                                                                                      (3, 4,  TRUE, FALSE, TRUE,  FALSE, FALSE),  -- receipt_detail
                                                                                                      (3, 6,  TRUE, FALSE, FALSE, FALSE, FALSE),  -- issue_list
                                                                                                      (3, 7,  TRUE, TRUE,  TRUE,  FALSE, FALSE),  -- issue_create
                                                                                                      (3, 8,  TRUE, FALSE, TRUE,  FALSE, FALSE),  -- issue_detail
                                                                                                      (3, 10, TRUE, FALSE, FALSE, FALSE, FALSE),  -- inventory_list
                                                                                                      (3, 11, TRUE, FALSE, FALSE, FALSE, FALSE);  -- inventory_history

-- Users mặc định (password: 123456 — đổi trước khi demo!)
INSERT INTO user (username, password_hash, full_name, role_id, email) VALUES
                                                                          ('admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4p8WY9y/FMxFZJ.g8RmlFCCHIB.a', 'Administrator',     1, 'admin@warehouse.com'),
                                                                          ('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4p8WY9y/FMxFZJ.g8RmlFCCHIB.a', 'Warehouse Manager',  2, 'manager@warehouse.com'),
                                                                          ('staff',   '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4p8WY9y/FMxFZJ.g8RmlFCCHIB.a', 'Warehouse Staff',    3, 'staff@warehouse.com');

INSERT INTO product_category (category_name, description) VALUES
                                                              ('Electronics',     'Thiết bị điện tử'),
                                                              ('Office Supplies', 'Văn phòng phẩm'),
                                                              ('Food & Beverage', 'Thực phẩm & đồ uống');

INSERT INTO vendor (vendor_code, vendor_name, contact_person, phone, email) VALUES
                                                                                ('V001', 'ABC Trading Co.',   'Nguyen Van A', '0901234567', 'contact@abctrading.com'),
                                                                                ('V002', 'XYZ Supplies Ltd.', 'Tran Thi B',   '0912345678', 'info@xyzsupplies.com');

INSERT INTO customer (customer_code, customer_name, contact_person, phone, email) VALUES
                                                                                      ('C001', 'Cong ty TNHH Alpha', 'Le Van C',   '0923456789', 'purchase@alpha.com'),
                                                                                      ('C002', 'Truong THPT Beta',   'Pham Thi D', '0934567890', 'admin@beta.edu.vn');

INSERT INTO product (product_code, barcode, product_name, unit, unit_price, category_id) VALUES
                                                                                             ('P001', '1234567890123', 'Laptop Dell Inspiron', 'Cái',  15000000, 1),
                                                                                             ('P002', '1234567890124', 'Mouse Logitech',        'Cái',   250000,  1),
                                                                                             ('P003', '1234567890125', 'Giấy A4',               'Ram',    80000,  2),
                                                                                             ('P004', '1234567890126', 'Bút bi',                'Hộp',    50000,  2);

INSERT INTO inventory (product_id, quantity)
SELECT product_id, 0 FROM product;

-- =====================================================
-- END OF SCRIPT
-- =====================================================