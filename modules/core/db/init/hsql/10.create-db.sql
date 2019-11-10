-- begin SYS_CATEGORY_ATTR
alter table SYS_CATEGORY_ATTR add column DTYPE varchar(100) ^
update SYS_CATEGORY_ATTR set DTYPE = 'dynattrext_CategoryAttribute' where DTYPE is null ^
-- end SYS_CATEGORY_ATTR
