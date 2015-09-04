/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.raft.protocol.response;

import net.kuujo.copycat.io.BufferInput;
import net.kuujo.copycat.io.BufferOutput;
import net.kuujo.copycat.io.serializer.SerializeWith;
import net.kuujo.copycat.io.serializer.Serializer;
import net.kuujo.copycat.raft.protocol.error.RaftError;
import net.kuujo.copycat.util.Assert;
import net.kuujo.copycat.util.BuilderPool;
import net.kuujo.copycat.util.ReferenceManager;

import java.util.Objects;

/**
 * Protocol publish response.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@SerializeWith(id=269)
public class PublishResponse extends SessionResponse<PublishResponse> {

  /**
   * The unique identifier for the publish response type.
   */
  public static final byte TYPE = 0x06;

  private static final BuilderPool<Builder, PublishResponse> POOL = new BuilderPool<>(Builder::new);

  /**
   * Returns a new publish response builder.
   *
   * @return A new publish response builder.
   */
  public static Builder builder() {
    return POOL.acquire();
  }

  /**
   * Returns a publish response builder for an existing response.
   *
   * @param response The response to build.
   * @return The publish response builder.
   * @throws NullPointerException if {@code response} is null
   */
  public static Builder builder(PublishResponse response) {
    return POOL.acquire(Assert.notNull(response, "response"));
  }

  private long sequence;

  /**
   * @throws NullPointerException if {@code referenceManager} is null
   */
  public PublishResponse(ReferenceManager<PublishResponse> referenceManager) {
    super(referenceManager);
  }

  @Override
  public byte type() {
    return TYPE;
  }

  /**
   * Returns the event sequence number.
   *
   * @return The event sequence number.
   */
  public long sequence() {
    return sequence;
  }

  @Override
  public void readObject(BufferInput buffer, Serializer serializer) {
    status = Status.forId(buffer.readByte());
    if (status == Status.OK) {
      error = null;
      sequence = buffer.readLong();
    } else {
      error = RaftError.forId(buffer.readByte());
    }
  }

  @Override
  public void writeObject(BufferOutput buffer, Serializer serializer) {
    buffer.writeByte(status.id());
    if (status == Status.OK) {
      buffer.writeLong(sequence);
    } else {
      buffer.writeByte(error.id());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), status);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof PublishResponse) {
      PublishResponse response = (PublishResponse) object;
      return response.status == status
        && response.sequence == sequence;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s[status=%s]", getClass().getSimpleName(), status);
  }

  /**
   * Publish response builder.
   */
  public static class Builder extends SessionResponse.Builder<Builder, PublishResponse> {

    protected Builder(BuilderPool<Builder, PublishResponse> pool) {
      super(pool, PublishResponse::new);
    }

    @Override
    protected void reset() {
      super.reset();
      response.sequence = 0;
    }

    /**
     * Sets the event sequence number.
     *
     * @param eventSequence The event sequence number.
     * @return The request builder.
     * @throws IllegalArgumentException if {@code eventSequence} is less than 1
     */
    public Builder withSequence(long eventSequence) {
      response.sequence = Assert.argNot(eventSequence, eventSequence < 1, "eventSequence cannot be less than 1");
      return this;
    }

    /**
     * @throws IllegalStateException if sequence is less than 1
     */
    @Override
    public PublishResponse build() {
      super.build();
      Assert.stateNot(response.sequence < 1, "sequence cannot be less than 1");
      return response;
    }

    @Override
    public int hashCode() {
      return Objects.hash(response);
    }

    @Override
    public boolean equals(Object object) {
      return object instanceof Builder && ((Builder) object).response.equals(response);
    }

    @Override
    public String toString() {
      return String.format("%s[response=%s]", getClass().getCanonicalName(), response);
    }

  }

}