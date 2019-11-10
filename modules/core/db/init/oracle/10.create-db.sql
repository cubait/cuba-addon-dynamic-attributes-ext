-- begin SYS_CATEGORY_ATTR
alter table SYS_CATEGORY_ATTR add ( DTYPE varchar2(100 char) ) ^
update SYS_CATEGORY_ATTR set DTYPE = 'dynattrext_CategoryAttribute' where DTYPE is null ^
-- end SYS_CATEGORY_ATTR
