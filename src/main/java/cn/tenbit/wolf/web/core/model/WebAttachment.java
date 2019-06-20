package cn.tenbit.wolf.web.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class WebAttachment implements Serializable {
    private static final long serialVersionUID = -7569321909900595670L;

    private String name;

    private byte[] content;
}
