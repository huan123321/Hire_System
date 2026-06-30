package Hiresys.mapper;

import Hiresys.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Users and global privileges Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-09-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
